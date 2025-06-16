package com.example.myeventmanagement;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SingleEvent extends AppCompatActivity {

    private ImageView singleImg;
    private TextView singleTitle, singleVenue, singleBudget, singleDesign, singleDate, singleName, singleContact;
    private Button sinBack, btnChat, btnReminder;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_event);

        initializeViews();
        displayEventDetails();
        setupButtonListeners();
    }

    private void initializeViews() {
        singleImg = findViewById(R.id.singleImg);
        singleTitle = findViewById(R.id.singleTitle);
        singleVenue = findViewById(R.id.singleVenue);
        singleBudget = findViewById(R.id.singleBudget);
        singleDesign = findViewById(R.id.singleDesign);
        singleDate = findViewById(R.id.singleDate);
        singleName = findViewById(R.id.singlename);
        singleContact = findViewById(R.id.singlecontact);
        sinBack = findViewById(R.id.sinBack);
        btnChat = findViewById(R.id.btnChat);
        btnReminder = findViewById(R.id.btnReminder);
    }

    private void displayEventDetails() {
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "Tadbir ma’lumotlari topilmadi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventId = intent.getStringExtra("eventId");
        String eventType = intent.getStringExtra("eventType");
        String eventVenue = intent.getStringExtra("eventVenue");
        String budget = intent.getStringExtra("budget");
        String design = intent.getStringExtra("design");
        String date = intent.getStringExtra("date");
        String name = intent.getStringExtra("name");
        String contact = intent.getStringExtra("contact");
        String uploadImage = intent.getStringExtra("uploadImage");

        setTextOrDefault(singleTitle, eventType, "Noma’lum tadbir");
        setTextOrDefault(singleVenue, eventVenue, "Manzil kiritilmagan");
        setTextOrDefault(singleBudget, budget, "Byudjet kiritilmagan");
        setTextOrDefault(singleDesign, design, "Dizayn kiritilmagan");
        setTextOrDefault(singleDate, date, "Sana kiritilmagan");
        setTextOrDefault(singleName, name, "Nomi kiritilmagan");
        setTextOrDefault(singleContact, contact, "Bog‘lanish kiritilmagan");

        if (uploadImage != null && !uploadImage.isEmpty()) {
            Glide.with(this).load(uploadImage).placeholder(R.drawable.birthday).error(R.drawable.birthday).into(singleImg);
        } else {
            singleImg.setImageResource(R.drawable.birthday);
        }
    }

    private void setupButtonListeners() {
        sinBack.setOnClickListener(v -> finish());

        btnChat.setOnClickListener(v -> {
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(this, "Chat uchun tadbir topilmadi", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent chatIntent = new Intent(SingleEvent.this, EventChatActivity.class);
            chatIntent.putExtra("eventId", eventId);
            chatIntent.putExtra("eventType", singleTitle.getText().toString());
            startActivity(chatIntent);
        });

        btnReminder.setOnClickListener(v -> addEventToCalendar());
    }

    private void setTextOrDefault(TextView textView, String text, String defaultText) {
        textView.setText(text != null && !text.isEmpty() ? text : defaultText);
    }

    private void addEventToCalendar() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CALENDAR}, 1);
            return;
        }

        String dateStr = singleDate.getText().toString();
        String title = singleTitle.getText().toString();
        String venue = singleVenue.getText().toString();

        if (dateStr.equals("Sana kiritilmagan")) {
            Toast.makeText(this, "Eslatma qo‘shish uchun sana kiritilmagan", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, title)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, venue)
                    .putExtra(CalendarContract.Events.ALL_DAY, true)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.getTimeInMillis() + 60 * 60 * 1000);

            startActivity(intent);
        } catch (ParseException e) {
            Toast.makeText(this, "Sana formati noto‘g‘ri: " + dateStr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addEventToCalendar();
        } else {
            Toast.makeText(this, "Kalendar ruxsati rad etildi", Toast.LENGTH_SHORT).show();
        }
    }
}