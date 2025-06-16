package com.example.myeventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivitylogin extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email, password;
    private DatabaseReference usersRef;
    Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activitylogin);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email_login = email.getText().toString().trim();
                String password_login = password.getText().toString().trim();

                if (!Email_login.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(Email_login).matches()) {
                    if (!password_login.isEmpty()) {
                        auth.signInWithEmailAndPassword(Email_login, password_login)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String userId = auth.getCurrentUser().getUid();
                                            checkUserRole(userId);
                                        } else {
                                            Toast.makeText(MainActivitylogin.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        password.setError("Parol bo'sh bo'lishi mumkin emas");
                    }
                } else {
                    email.setError("To'g'ri email kiriting");
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivitylogin.this, MainActivitysignup.class));
            }
        });
    }

    private void checkUserRole(String userId) {
        usersRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String role = snapshot.child("role").getValue(String.class);

                if ("admin".equals(role)) {
                    Toast.makeText(MainActivitylogin.this, "Admin sifatida kirdingiz", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivitylogin.this, AdminActivity.class));
                } else {
                    Toast.makeText(MainActivitylogin.this, "Foydalanuvchi sifatida kirdingiz", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivitylogin.this, MainActivity.class));
                }
                finish();
            } else {
                Toast.makeText(MainActivitylogin.this, "Foydalanuvchi ma'lumotlari topilmadi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
