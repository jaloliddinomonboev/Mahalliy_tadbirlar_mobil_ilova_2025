package com.example.myeventmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerEvents;
    private FloatingActionButton fabAddEvent;
    private CalendarView calendarView;
    private TextView tvStats, tvJoinUs;
    private LinearLayout eventDotsContainer;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private DatabaseReference eventsRef, userRef;
    private HomeAdapter homeAdapter;
    private ArrayList<UploadEvent> eventList;
    private ValueEventListener eventsListener;
    private int currentPosition = 0;
    private Timer timer;
    private boolean isAdmin = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("Event");
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid());

        recyclerEvents = view.findViewById(R.id.recyclerEvents);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        calendarView = view.findViewById(R.id.calendarView);
        tvStats = view.findViewById(R.id.tvStats);
        tvJoinUs = view.findViewById(R.id.tvJoinUs);
        eventDotsContainer = view.findViewById(R.id.event_dots_container);
        progressBar = view.findViewById(R.id.progressBar);
        toolbar = view.findViewById(R.id.toolbar);

        // Toolbar sozlamalari
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(""); // Toolbar’da sarlavha o‘chirildi

        recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        eventList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getContext(), eventList,
                event -> {
                    Intent intent = new Intent(getActivity(), SingleEvent.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("eventType", event.getEventType());
                    intent.putExtra("eventVenue", event.getEventVenue());
                    intent.putExtra("budget", event.getBudget());
                    intent.putExtra("design", event.getDesign());
                    intent.putExtra("date", event.getDate());
                    intent.putExtra("name", event.getName());
                    intent.putExtra("contact", event.getContact());
                    intent.putExtra("uploadImage", event.getUploadImage());
                    Log.d("HomeFragment", "Sending Event ID: " + event.getEventId());
                    startActivity(intent);
                },
                event -> {
                    if (isAdmin) {
                        deleteEvent(event.getEventId());
                    } else {
                        Toast.makeText(getContext(), "Faqat adminlar o‘chira oladi", Toast.LENGTH_SHORT).show();
                    }
                }, isAdmin);
        recyclerEvents.setAdapter(homeAdapter);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerEvents);

        loadEvents();
        loadStats();
        startAutoScroll();

        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(getActivity(), Register.class)));

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d-%02d-%d", dayOfMonth, month + 1, year);
            filterEventsByDate(selectedDate);
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent intent = new Intent(getActivity(), AboutAppActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_contact) {
            String telegramUrl = "https://t.me/jaloliddin_1308";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl));
            intent.setPackage("org.telegram.messenger");

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Telegram ilovasi o‘rnatilmagan, brauzer orqali ochamiz
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl));
                startActivity(browserIntent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        Query recentEventsQuery = eventsRef.limitToLast(10);
        eventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                ArrayList<UploadEvent> tempList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UploadEvent event = data.getValue(UploadEvent.class);
                    if (event != null) {
                        String eventId = data.getKey();
                        event.setEventId(eventId);
                        tempList.add(event);
                        Log.d("HomeFragment", "Loaded Event ID: " + eventId);
                    }
                }

                Collections.sort(tempList, new Comparator<UploadEvent>() {
                    @Override
                    public int compare(UploadEvent e1, UploadEvent e2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        try {
                            Date date1 = sdf.parse(e1.getDate());
                            Date date2 = sdf.parse(e2.getDate());
                            return date2.compareTo(date1);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });

                eventList.addAll(tempList);
                homeAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Xatolik: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        recentEventsQuery.addValueEventListener(eventsListener);
    }

    private void loadStats() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int eventsCreated = snapshot.child("eventsCreated").getValue(Integer.class) != null ?
                        snapshot.child("eventsCreated").getValue(Integer.class) : 0;
                tvStats.setText("Bu oyda: " + eventsCreated + " ta tadbir");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Statistika yuklanmadi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEventsByDate(String date) {
        ArrayList<UploadEvent> filteredList = new ArrayList<>();
        for (UploadEvent event : eventList) {
            if (event.getDate() != null && event.getDate().equals(date)) {
                filteredList.add(event);
            }
        }
        homeAdapter.updateList(filteredList);
        currentPosition = 0;
    }

    private void startAutoScroll() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null && eventList.size() > 0) {
                    if (currentPosition < eventList.size() - 1) {
                        currentPosition++;
                    } else {
                        currentPosition = 0;
                    }
                    getActivity().runOnUiThread(() -> recyclerEvents.smoothScrollToPosition(currentPosition));
                }
            }
        }, 0, 3000);
    }

    private void deleteEvent(String eventId) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Tadbirni o‘chirish")
                .setMessage("Ushbu tadbirni o‘chirishni tasdiqlaysizmi?")
                .setPositiveButton("Ha", (dialog, which) -> {
                    eventsRef.child(eventId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Tadbir o‘chirildi", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Yo‘q", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventsListener != null) {
            eventsRef.removeEventListener(eventsListener);
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}