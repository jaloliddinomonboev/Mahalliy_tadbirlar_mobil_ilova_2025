package com.example.myeventmanagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private DatabaseReference databaseReference, currentUserRef;
    private List<User> userList, filteredList;
    private UserAdapter userAdapter;
    private FirebaseAuth auth;
    private ValueEventListener usersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        currentUserRef = databaseReference.child(auth.getCurrentUser().getUid());

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        userAdapter = new UserAdapter(filteredList, unused -> loadUsers());
        usersRecyclerView.setAdapter(userAdapter);

        setupSearchView();
        checkAdminRole();
    }

    private void checkAdminRole() {
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);
                if (role != null && role.equals("admin")) {
                    loadUsers();
                } else {
                    Toast.makeText(AdminActivity.this, "Sizda admin huquqlari yo‘q", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        if (usersListener != null) {
            databaseReference.removeEventListener(usersListener);
        }
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                filteredList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUid(userSnapshot.getKey());
                        userList.add(user);
                    }
                }
                filteredList.addAll(userList);
                userAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (filteredList.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Foydalanuvchilar yo‘q", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminActivity.this, "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(usersListener);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery) ||
                        user.getPhone().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersListener != null) {
            databaseReference.removeEventListener(usersListener);
        }
    }
}