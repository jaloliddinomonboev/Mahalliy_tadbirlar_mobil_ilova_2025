package com.example.myeventmanagement;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.function.Consumer;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private DatabaseReference databaseReference;
    private Consumer<Void> onDataChangedListener;
    private Context context;

    public UserAdapter(List<User> userList, Consumer<Void> onDataChangedListener) {
        this.userList = userList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        this.onDataChangedListener = onDataChangedListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userPhone.setText(user.getPhone());

        // Holatni ko‘rsatish va tugmalar holatini boshqarish
        updateButtonVisibility(holder, user);

        // Tasdiqlash tugmasi
        holder.approveButton.setOnClickListener(v -> {
            Log.d("UserAdapter", "Approving user: " + user.getUid());
            databaseReference.child(user.getUid()).child("isApproved").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Foydalanuvchi tasdiqlandi", Toast.LENGTH_SHORT).show();
                        Log.d("UserAdapter", "User approved successfully: " + user.getUid());
                        user.setApproved(true); // Lokal obyektni yangilash
                        updateButtonVisibility(holder, user); // Tugmalar holatini yangilash
                        notifyItemChanged(position); // Elementni yangilash
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserAdapter", "Failed to approve user: " + e.getMessage());
                    });
        });

        // Olib tashlash tugmasi
        holder.rejectButton.setOnClickListener(v -> {
            Log.d("UserAdapter", "Rejecting user: " + user.getUid());
            databaseReference.child(user.getUid()).child("isApproved").setValue(false)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Tasdiq olib tashlandi", Toast.LENGTH_SHORT).show();
                        Log.d("UserAdapter", "User rejected successfully: " + user.getUid());
                        user.setApproved(false); // Lokal obyektni yangilash
                        updateButtonVisibility(holder, user); // Tugmalar holatini yangilash
                        notifyItemChanged(position); // Elementni yangilash
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserAdapter", "Failed to reject user: " + e.getMessage());
                    });
        });
    }

    private void updateButtonVisibility(UserViewHolder holder, User user) {
        if (user.isApproved()) {
            holder.statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.holo_green_dark));
            holder.approveButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setEnabled(true);
            holder.rejectButton.setText("Olib tashlash");
        } else {
            holder.statusIndicator.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.holo_red_dark));
            holder.approveButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.approveButton.setEnabled(true);
            holder.approveButton.setText("Tasdiqlash");
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userPhone;
        Button approveButton, rejectButton;
        View statusIndicator;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userPhone = itemView.findViewById(R.id.userPhone);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}