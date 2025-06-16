package com.example.myeventmanagement;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class EventChatActivity extends AppCompatActivity {

    private TextView eventChatTitle;
    private RecyclerView eventChatRecyclerView;
    private EditText eventMessageInput;
    private ImageButton eventSendButton;
    private EventChatAdapter eventChatAdapter;
    private ArrayList<EventChatMessage> eventMessageList;
    private DatabaseReference eventChatRef;
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_chat);

        eventChatTitle = findViewById(R.id.eventChatTitle);
        eventChatRecyclerView = findViewById(R.id.eventChatRecyclerView);
        eventMessageInput = findViewById(R.id.eventMessageInput);
        eventSendButton = findViewById(R.id.eventSendButton);

        String eventId = getIntent().getStringExtra("eventId");
        String eventType = getIntent().getStringExtra("eventType");
        eventChatTitle.setText(eventType + " uchun chat");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventMessageList = new ArrayList<>();
        eventChatAdapter = new EventChatAdapter(eventMessageList, currentUserId);
        eventChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventChatRecyclerView.setAdapter(eventChatAdapter);

        eventChatRef = FirebaseDatabase.getInstance().getReference("EventChats").child(eventId);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        loadEventMessages();

        eventSendButton.setOnClickListener(v -> sendEventMessage());
    }

    private void loadEventMessages() {
        eventChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventMessageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    EventChatMessage message = data.getValue(EventChatMessage.class);
                    if (message != null) {
                        eventMessageList.add(message);
                    }
                }
                eventChatAdapter.notifyDataSetChanged();
                eventChatRecyclerView.scrollToPosition(eventMessageList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EventChatActivity.this, "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEventMessage() {
        String messageText = eventMessageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String profileImage = snapshot.child("profileImageUrl").getValue(String.class);

                    EventChatMessage message = new EventChatMessage(
                            currentUserId,
                            messageText,
                            System.currentTimeMillis(),
                            userName != null ? userName : "Anonim",
                            profileImage
                    );
                    eventChatRef.push().setValue(message);
                    eventMessageInput.setText("");
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(EventChatActivity.this, "Foydalanuvchi ma’lumotlari olinmadi", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}