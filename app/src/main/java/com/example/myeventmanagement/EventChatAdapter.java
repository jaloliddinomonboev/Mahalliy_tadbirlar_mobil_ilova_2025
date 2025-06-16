package com.example.myeventmanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventChatAdapter extends RecyclerView.Adapter<EventChatAdapter.EventChatViewHolder> {

    private ArrayList<EventChatMessage> messageList;
    private String currentUserId;

    public EventChatAdapter(ArrayList<EventChatMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public EventChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_chat_item, parent, false);
        return new EventChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventChatViewHolder holder, int position) {
        EventChatMessage message = messageList.get(position);
        boolean isCurrentUser = message.getUserId().equals(currentUserId);

        // Xabar joylashuvi
        holder.messageContainer.setGravity(isCurrentUser ? android.view.Gravity.END : android.view.Gravity.START);
        holder.messageBubble.setBackgroundResource(
                isCurrentUser ? R.drawable.chat_bubble_right : R.drawable.chat_bubble_left
        );

        // Foydalanuvchi ma’lumotlari
        holder.userNameText.setText(message.getUserName());
        holder.messageText.setText(message.getMessage());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));
        holder.messageTime.setText(time);

        if (message.getProfileImage() != null && !message.getProfileImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(message.getProfileImage())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class EventChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        LinearLayout messageBubble;
        ImageView profileImage;
        TextView userNameText, messageText, messageTime;

        EventChatViewHolder(View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageBubble = itemView.findViewById(R.id.messageBubble);
            profileImage = itemView.findViewById(R.id.profileImage);
            userNameText = itemView.findViewById(R.id.userNameText);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}