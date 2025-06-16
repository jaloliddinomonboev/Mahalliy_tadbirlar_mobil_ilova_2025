package com.example.myeventmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications, switchReminders;
    private Button btnBack;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchReminders = findViewById(R.id.switchReminders);
        btnBack = findViewById(R.id.btnBack);

        userRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Sozlamalarni yuklash
        loadSettings();

        // O‘zgarishlarni saqlash
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userRef.child("settings").child("notificationsEnabled").setValue(isChecked);
        });

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userRef.child("settings").child("remindersEnabled").setValue(isChecked);
        });

        // Orqaga qaytish
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        userRef.child("settings").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Boolean notificationsEnabled = snapshot.child("notificationsEnabled").getValue(Boolean.class);
                Boolean remindersEnabled = snapshot.child("remindersEnabled").getValue(Boolean.class);

                switchNotifications.setChecked(notificationsEnabled != null && notificationsEnabled);
                switchReminders.setChecked(remindersEnabled != null && remindersEnabled);
            } else {
                // Default qiymatlar
                switchNotifications.setChecked(true);
                switchReminders.setChecked(true);
            }
        });
    }
}