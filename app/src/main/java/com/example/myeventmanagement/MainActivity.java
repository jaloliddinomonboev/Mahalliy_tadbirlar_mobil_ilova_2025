package com.example.myeventmanagement;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myeventmanagement.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase autentifikatsiyani tekshirish
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Foydalanuvchi tizimga kirmagan, LoginActivity ga yo‘naltiramiz
            startActivity(new Intent(MainActivity.this, MainActivitylogin.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.Home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.Event) {
                replaceFragment(new EventFragment());
                return true;
            } else if (itemId == R.id.Service) {
                replaceFragment(new ServiceFragment());
                return true;
            } else if (itemId == R.id.Profile) {
                replaceFragment(new ProfileFragment());
                return true;
            } else if (itemId == R.id.Chat) {
                replaceFragment(new ChatFragment());
                return true;
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
