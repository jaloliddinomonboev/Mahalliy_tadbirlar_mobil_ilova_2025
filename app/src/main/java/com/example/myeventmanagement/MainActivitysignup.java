package com.example.myeventmanagement;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivitysignup extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private EditText name, emailsignup, passwordsignup, phone;
    private Button signup, returnbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activitysignup);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        signup = findViewById(R.id.signup);
        returnbutton = findViewById(R.id.returnbutton);
        emailsignup = findViewById(R.id.emailsignup);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        passwordsignup = findViewById(R.id.passwordsignup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailsignup.getText().toString().trim();
                String userPassword = passwordsignup.getText().toString().trim();
                String userName = name.getText().toString().trim();
                String userPhone = phone.getText().toString().trim();

                if (userEmail.isEmpty()) {
                    emailsignup.setError("Email cannot be empty");
                    return;
                }
                if (userPassword.isEmpty()) {
                    passwordsignup.setError("Password cannot be empty");
                    return;
                }
                if (userName.isEmpty()) {
                    name.setError("Name cannot be empty");
                    return;
                }
                if (userPhone.isEmpty()) {
                    phone.setError("Phone number cannot be empty");
                    return;
                }

                // Foydalanuvchini Firebase Authentication-ga qo‘shish
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = auth.getCurrentUser().getUid();

                                    // Yangi foydalanuvchi ma’lumotlarini yaratish
                                    User newUser = new User(userId, userName, userEmail, userPhone, "", false, "user");

                                    // Ma’lumotlarni Firebase Realtime Database-ga saqlash
                                    databaseReference.child(userId).setValue(newUser)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> dbTask) {
                                                    if (dbTask.isSuccessful()) {
                                                        Toast.makeText(MainActivitysignup.this, "Successfully Signed Up", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(MainActivitysignup.this, MainActivitylogin.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(MainActivitysignup.this, "Database Error", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(MainActivitysignup.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        returnbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivitysignup.this, MainActivitylogin.class));
            }
        });
    }
}
