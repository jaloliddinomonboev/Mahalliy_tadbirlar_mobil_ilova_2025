package com.example.myeventmanagement;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;

    private Context context;
    private List<Object> chatItems; // Xabarlar va kun sarlavhalari uchun umumiy ro‘yxat
    private String currentUserId;
    private String currentUserRole;

    public ChatAdapter(Context context, List<Object> chatItems, String currentUserId, String currentUserRole) {
        this.context = context;
        this.chatItems = chatItems;
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = chatItems.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_DATE_HEADER;
        } else {
            ChatMessage chatMessage = (ChatMessage) item;
            return chatMessage.getSenderId().equals(currentUserId) ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view;
            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_right, parent, false);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_left, parent, false);
            }
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = chatItems.get(position);
        if (holder instanceof DateHeaderViewHolder) {
            DateHeaderViewHolder dateHolder = (DateHeaderViewHolder) holder;
            dateHolder.txtDate.setText((String) item);
        } else {
            ChatViewHolder chatHolder = (ChatViewHolder) holder;
            ChatMessage chatMessage = (ChatMessage) item;

            chatHolder.txtMessage.setText(chatMessage.getText());
            chatHolder.txtSenderName.setText(chatMessage.getSenderName());

            // Vaqtni formatlash (soat va daqiqa)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String formattedTime = timeFormat.format(new Date(chatMessage.getTimestamp()));
            chatHolder.txtTime.setText(formattedTime);

            if (chatMessage.getSenderProfile() != null && !chatMessage.getSenderProfile().isEmpty()) {
                Glide.with(context)
                        .load(chatMessage.getSenderProfile())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(chatHolder.imgProfile);
            } else {
                chatHolder.imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Xabarni uzun bosganda admin o‘chirishi mumkin
            chatHolder.itemView.setOnLongClickListener(v -> {
                if ("admin".equals(currentUserRole)) {
                    new AlertDialog.Builder(context)
                            .setTitle("Xabarni o'chirish")
                            .setMessage("Bu xabarni o‘chirishni xohlaysizmi?")
                            .setPositiveButton("Ha", (dialog, which) -> {
                                FirebaseDatabase.getInstance().getReference("chats")
                                        .child(chatMessage.getMessageId())
                                        .removeValue();
                            })
                            .setNegativeButton("Yo‘q", null)
                            .show();
                }
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtSenderName, txtMessage, txtTime;
        ImageView imgProfile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}