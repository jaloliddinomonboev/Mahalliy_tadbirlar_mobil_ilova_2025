package com.example.myeventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventFragment extends Fragment {

    private Button upload;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ArrayList<UploadEvent> recycleList;
    private ArrayList<UploadEvent> filteredList;
    private FirebaseDatabase firebaseDatabase;
    private EventAdapter recyclerAdapter;
    private FirebaseAuth mAuth;
    private boolean isAdmin = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        upload = view.findViewById(R.id.upload);
        recyclerView = view.findViewById(R.id.recycleView);
        searchView = view.findViewById(R.id.searchView);
        recycleList = new ArrayList<>();
        filteredList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        recyclerAdapter = new EventAdapter(filteredList, getContext(), event -> {
            Intent intent = new Intent(getActivity(), SingleEvent.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventType", event.getEventType());
            intent.putExtra("eventVenue", event.getEventVenue());
            intent.putExtra("budget", event.getBudget());
            intent.putExtra("design", event.getDesign());
            intent.putExtra("date", event.getDate());
            intent.putExtra("name", event.getName());
            intent.putExtra("contact", event.getContact());
            intent.putExtra("uploadImage", event.getUploadImage());
            Log.d("EventFragment", "Sending Event ID: " + event.getEventId());
            startActivity(intent);
        }, event -> deleteEvent(event.getEventId()), isAdmin);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(recyclerAdapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        checkAdminAndEnableDelete();
        loadEvents();

        upload.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Register.class);
            startActivity(intent);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });

        return view;
    }

    private void loadEvents() {
        firebaseDatabase.getReference().child("Event").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recycleList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UploadEvent uploadEvent = dataSnapshot.getValue(UploadEvent.class);
                    if (uploadEvent != null) {
                        uploadEvent.setEventId(dataSnapshot.getKey());
                        recycleList.add(uploadEvent);
                        Log.d("EventFragment", "Loaded Event ID: " + dataSnapshot.getKey());
                    }
                }
                filteredList.clear();
                filteredList.addAll(recycleList);
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEvents(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(recycleList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (UploadEvent event : recycleList) {
                if (event.getEventType() != null && event.getEventType().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(event);
                }
            }
        }
        recyclerAdapter.notifyDataSetChanged();
    }

    private void checkAdminAndEnableDelete() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            firebaseDatabase.getReference("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && "admin".equals(user.getRole())) {
                        isAdmin = true;
                        recyclerAdapter = new EventAdapter(filteredList, getContext(), event -> {
                            Intent intent = new Intent(getActivity(), SingleEvent.class);
                            intent.putExtra("eventId", event.getEventId());
                            intent.putExtra("eventType", event.getEventType());
                            intent.putExtra("eventVenue", event.getEventVenue());
                            intent.putExtra("budget", event.getBudget());
                            intent.putExtra("design", event.getDesign());
                            intent.putExtra("date", event.getDate());
                            intent.putExtra("name", event.getName());
                            intent.putExtra("contact", event.getContact());
                            intent.putExtra("uploadImage", event.getUploadImage());
                            startActivity(intent);
                        }, event -> {
                            if (isAdmin) {
                                deleteEvent(event.getEventId());
                            } else {
                                Toast.makeText(getContext(), "Faqat adminlar o‘chira oladi", Toast.LENGTH_SHORT).show();
                            }
                        }, isAdmin);
                        recyclerView.setAdapter(recyclerAdapter);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("EventFragment", "Rol tekshirishda xatolik: " + error.getMessage());
                }
            });
        }
    }

    private void deleteEvent(String eventId) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Tadbirni o‘chirish")
                .setMessage("Ushbu tadbirni o‘chirishni tasdiqlaysizmi?")
                .setPositiveButton("Ha", (dialog, which) -> {
                    firebaseDatabase.getReference("Event").child(eventId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Tadbir o‘chirildi", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Yo‘q", null)
                .show();
    }
}