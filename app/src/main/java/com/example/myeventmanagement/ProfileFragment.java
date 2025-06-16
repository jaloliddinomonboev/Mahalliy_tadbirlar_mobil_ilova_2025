package com.example.myeventmanagement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private EditText etName, etEmail, etPhone;
    private Button btnUploadProfileImage, btnUpdateProfile, btnLogout, btnChangePassword;
    private ImageButton btnNotifications, btnSettings;
    private RecyclerView notificationsRecyclerView;
    private ProgressBar progressBar;
    private TextView tvStats;
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationModel> notificationList;

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private FirebaseUser user;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // UI elementlar
        profileImageView = view.findViewById(R.id.profileImageView);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        btnUploadProfileImage = view.findViewById(R.id.btnUploadProfileImage);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvStats = view.findViewById(R.id.tvStats);

        // RecyclerView sozlash
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter();
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Foydalanuvchi ma'lumotlarini yuklash
        loadUserProfile();

        // Profil rasm yuklash
        btnUploadProfileImage.setOnClickListener(v -> openFileChooser());

        // Profil ma’lumotlarini yangilash
        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        // Chiqish
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(getActivity(), "Ilovadan chiqildi", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), MainActivitylogin.class));
            getActivity().finish();
        });

        // Parolni o‘zgartirish
        btnChangePassword.setOnClickListener(v -> resetPassword());

        // Sozlamalar
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        // Bildirishnomalarni ko‘rsatish
        btnNotifications.setOnClickListener(v -> toggleNotifications());

        return view;
    }

    private void loadUserProfile() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    Integer eventsCreated = snapshot.child("eventsCreated").getValue(Integer.class);
                    Integer eventsAttended = snapshot.child("eventsAttended").getValue(Integer.class);

                    etName.setText(name);
                    etEmail.setText(email);
                    etPhone.setText(phone);

                    if (profileImageUrl != null) {
                        Glide.with(requireContext()).load(profileImageUrl).into(profileImageView);
                    }

                    // Statistika ko‘rsatish
                    int created = eventsCreated != null ? eventsCreated : 0;
                    int attended = eventsAttended != null ? eventsAttended : 0;
                    String stats = "Tadbirlarni yaratdi: " + created + " | Ishtirok etdi: " + attended;
                    tvStats.setText(stats);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Profil yuklanmadi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(user.getUid() + ".jpg");
            fileReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        reference.child("profileImageUrl").setValue(uri.toString());
                        Toast.makeText(getActivity(), "Rasm yangilandi", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(getActivity(), "Rasm yuklanmadi", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProfile() {
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty()) {
            etName.setError("Ism bo‘sh bo‘lmasligi kerak");
            return;
        }
        if (newPhone.isEmpty()) {
            etPhone.setError("Telefon bo‘sh bo‘lmasligi kerak");
            return;
        }

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", newName);
        updateMap.put("phone", newPhone);

        reference.updateChildren(updateMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Ma'lumotlar yangilandi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Yangilashda xatolik", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword() {
        if (user != null) {
            auth.sendPasswordResetEmail(user.getEmail()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Parol tiklash emaili yuborildi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Email yuborishda xatolik", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void toggleNotifications() {
        if (notificationsRecyclerView.getVisibility() == View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
            loadNotifications();
            notificationsRecyclerView.setVisibility(View.VISIBLE);
            notificationsRecyclerView.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left)
            );
        } else {
            notificationsRecyclerView.setVisibility(View.GONE);
            notificationsRecyclerView.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right)
            );
        }
    }

    private void loadNotifications() {
        FirebaseDatabase.getInstance().getReference("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            NotificationModel notif = data.getValue(NotificationModel.class);
                            if (notif != null) {
                                notificationList.add(notif);
                            }
                        }
                        notificationAdapter.setNotifications(notificationList);
                        progressBar.setVisibility(View.GONE);
                        if (notificationList.isEmpty()) {
                            Toast.makeText(getActivity(), "Bildirishnomalar yo‘q", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}