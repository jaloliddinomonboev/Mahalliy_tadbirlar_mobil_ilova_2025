package com.example.myeventmanagement;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {
    private TextView eventType, date, design, budget, expDesign, name, contact, eventVenue;
    private ImageView uploadbtn, uploadImage;
    private Button submit, back01;
    private Uri ImageUri;
    private RelativeLayout relativeLayout;
    private AlertDialog dialog;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private boolean isApproved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFirebase();
        setupUI();
        setupListeners();
        checkUserApproval();
    }

    private void initializeFirebase() {
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupUI() {
        eventType = findViewById(R.id.eventType);
        date = findViewById(R.id.date);
        design = findViewById(R.id.design);
        budget = findViewById(R.id.budget);
        expDesign = findViewById(R.id.expDesign);
        name = findViewById(R.id.name);
        contact = findViewById(R.id.contact);
        eventVenue = findViewById(R.id.eventVenue);
        uploadbtn = findViewById(R.id.uploadbtn);
        uploadImage = findViewById(R.id.uploadImage);
        relativeLayout = findViewById(R.id.relative);
        submit = findViewById(R.id.submit);
        back01 = findViewById(R.id.back01);

        // Progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Wait...")
                .setTitle("Event Uploading")
                .setCancelable(false);
        dialog = builder.create();
    }

    private void setupListeners() {
        back01.setOnClickListener(v -> onBackPressed());

        uploadbtn.setOnClickListener(v -> {
            requestStoragePermission();
            relativeLayout.setVisibility(View.VISIBLE);
            uploadbtn.setVisibility(View.GONE);
        });

        date.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Register.this,
                    (view1, year1, month1, dayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, (month1 + 1), year1);
                        date.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        submit.setOnClickListener(v -> {
            if (isApproved) {
                uploadEventData();
            } else {
                Toast.makeText(this, "Sizga tadbir yuklashga ruxsat berilmagan!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserApproval() {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("Users").child(userId);

        userRef.child("isApproved").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Boolean approvalStatus = dataSnapshot.getValue(Boolean.class);
                isApproved = approvalStatus != null && approvalStatus;
                submit.setEnabled(isApproved);
                if (!isApproved) {
                    Toast.makeText(Register.this, "Admin sizga ruxsat bermagan!", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Register.this, "Foydalanuvchi ma'lumotlarini olishda xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void requestStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        Dexter.withContext(this)
                .withPermission(permission)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        pickImageFromGallery();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(Register.this, "Ruxsat berilmadi! Sozlamalardan ruxsat bering.", Toast.LENGTH_SHORT).show();
                        openAppSettings();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            if (ImageUri != null) {
                Picasso.get().load(ImageUri).into(uploadImage);
            }
        }
    }

    private void uploadEventData() {
        if (ImageUri == null) {
            Toast.makeText(this, "Iltimos, rasm tanlang!", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateStr = date.getText().toString();
        if (!isValidDateFormat(dateStr)) {
            Toast.makeText(this, "Vaqtni to'g'ri kiriting (dd-MM-yyyy) va kelajakdagi sana bo‘lsin!", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.show();

        StorageReference reference = firebaseStorage.getReference().child("Event")
                .child(System.currentTimeMillis() + ".jpg");

        reference.putFile(ImageUri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
            UploadEvent model = new UploadEvent();
            model.setUploadImage(uri.toString());
            model.setEventType(eventType.getText().toString());
            model.setDate(dateStr);
            model.setDesign(design.getText().toString());
            model.setBudget(budget.getText().toString());
            model.setExpDesign(expDesign.getText().toString());
            model.setName(name.getText().toString());
            model.setContact(contact.getText().toString());
            model.setEventVenue(eventVenue.getText().toString());

            String eventId = database.getReference("Event").push().getKey();
            model.setEventId(eventId); // eventId ni o‘rnatish

            database.getReference("Event").child(eventId).setValue(model)
                    .addOnSuccessListener(unused -> {
                        dialog.dismiss();
                        Toast.makeText(Register.this, "Tadbir muvaffaqiyatli yuklandi!", Toast.LENGTH_SHORT).show();
                        // Statistika yangilash
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        userRef.child("eventsCreated").get().addOnSuccessListener(snapshot -> {
                            int currentCount = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                            userRef.child("eventsCreated").setValue(currentCount + 1);
                        });
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(Register.this, "Yuklashda xatolik: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        })).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(Register.this, "Rasm yuklashda xatolik: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date parsedDate = sdf.parse(date);
            return parsedDate != null && !parsedDate.before(new Date()); // Kelajakdagi sanalarni qabul qilish
        } catch (ParseException e) {
            return false;
        }
    }

    private void scheduleNotification(String date, String eventName) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date eventDate = dateFormat.parse(date);
            if (eventDate == null) return;

            Calendar calendar = Calendar.getInstance();
            long currentTime = System.currentTimeMillis();

            String[] reminders = {
                    "3 kun qoldi|Tadbirga 3 kun qoldi: " + eventName,
                    "2 kun qoldi|Tadbirga 2 kun qoldi: " + eventName,
                    "Bugun tadbir|Bugun tadbir kuni: " + eventName
            };
            int[] daysBefore = {3, 2, 0};

            for (int i = 0; i < reminders.length; i++) {
                String[] parts = reminders[i].split("\\|");
                String title = parts[0];
                String message = parts[1];

                calendar.setTime(eventDate);
                calendar.add(Calendar.DAY_OF_MONTH, -daysBefore[i]);
                long delay = calendar.getTimeInMillis() - currentTime;

                if (delay > 0) {
                    setWorkManager(delay, title, message);
                }
            }

            sendFCMToAllUsers(eventName, eventDate);

        } catch (ParseException e) {
            Toast.makeText(this, "Sana formati noto‘g‘ri: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setWorkManager(long delay, String title, String message) {
        Data data = new Data.Builder()
                .putString("title", title)
                .putString("message", message)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationScheduler.class)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void sendFCMToAllUsers(String eventName, Date eventDate) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String token = userSnapshot.child("fcmToken").getValue(String.class);
                    if (token != null) {
                        scheduleFCMNotification(token, eventName, eventDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FCMError", "Foydalanuvchilarni olishda xatolik: " + error.getMessage());
            }
        });
    }

    private void scheduleFCMNotification(String token, String eventName, Date eventDate) {
        String[] messages = {
                eventName + " tadbiriga 3 kun qoldi!",
                eventName + " tadbiriga 2 kun qoldi!",
                eventName + " bugun bo'lib o'tadi!"
        };
        int[] daysBefore = {3, 2, 0};

        for (int i = 0; i < messages.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(eventDate);
            calendar.add(Calendar.DAY_OF_MONTH, -daysBefore[i]);
            long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

            if (delay > 0) {
                Data data = new Data.Builder()
                        .putString("token", token)
                        .putString("title", "Tadbir Eslatmasi")
                        .putString("message", messages[i])
                        .build();

                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FCMNotificationWorker.class)
                        .setInputData(data)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

                WorkManager.getInstance(this).enqueue(workRequest);
            }
        }
    }
}