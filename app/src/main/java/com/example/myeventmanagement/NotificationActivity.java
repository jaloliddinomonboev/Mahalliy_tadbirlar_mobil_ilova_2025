package com.example.myeventmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.notificationRecyclerView);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        recyclerView.setAdapter(adapter);

        loadNotifications();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        FirebaseDatabase.getInstance().getReference("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ArrayList<NotificationModel> notifications = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            NotificationModel notif = data.getValue(NotificationModel.class);
                            if (notif != null) {
                                notifications.add(notif);
                            }
                        }
                        adapter.setNotifications(notifications);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(NotificationActivity.this, "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}