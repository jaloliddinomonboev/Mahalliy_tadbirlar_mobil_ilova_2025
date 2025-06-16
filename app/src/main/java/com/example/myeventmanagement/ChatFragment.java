package com.example.myeventmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditText edtMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private List<Object> chatItems; // Xabarlar va kun sarlavhalari uchun
    private List<ChatMessage> chatMessages; // Faqat xabarlar uchun
    private DatabaseReference chatRef, userRef;
    private FirebaseUser currentUser;
    private String currentUserRole;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerChat);
        edtMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatMessages = new ArrayList<>();
        chatItems = new ArrayList<>();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return view;
        }

        chatRef = FirebaseDatabase.getInstance().getReference("chats");
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserRole = snapshot.child("role").getValue(String.class);
                if (currentUserRole == null) currentUserRole = "user";

                chatAdapter = new ChatAdapter(getContext(), chatItems, currentUser.getUid(), currentUserRole);
                recyclerView.setAdapter(chatAdapter);
                loadMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                chatItems.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        chatMessages.add(chatMessage);
                    }
                }

                // Xabarlarni kunlar bo‘yicha guruhlash
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                String lastDate = null;

                for (ChatMessage message : chatMessages) {
                    String messageDate = dateFormat.format(new Date(message.getTimestamp()));
                    if (!messageDate.equals(lastDate)) {
                        chatItems.add(messageDate); // Kun sarlavhasi qo‘shiladi
                        lastDate = messageDate;
                    }
                    chatItems.add(message); // Xabar qo‘shiladi
                }

                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatItems.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage() {
        String messageText = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String senderName = snapshot.child("name").getValue(String.class);
                String senderProfile = snapshot.child("profileImageUrl").getValue(String.class);
                String senderRole = snapshot.child("role").getValue(String.class);

                if (senderName == null) senderName = "User";
                if (senderProfile == null) senderProfile = "";
                if (senderRole == null) senderRole = "user";

                String messageId = chatRef.push().getKey();

                ChatMessage chatMessage = new ChatMessage(
                        messageId,
                        currentUser.getUid(),
                        senderName,
                        senderProfile,
                        senderRole,
                        messageText,
                        System.currentTimeMillis()
                );
                chatRef.child(messageId).setValue(chatMessage);
                edtMessage.setText("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}