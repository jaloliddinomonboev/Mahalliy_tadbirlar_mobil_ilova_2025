package com.example.myeventmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.EventViewHolder> {
    private Context context;
    private ArrayList<UploadEvent> eventList;
    private OnEventClickListener onEventClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private boolean isAdmin;

    // Interfeyslar aniqlanadi
    public interface OnEventClickListener {
        void onEventClick(UploadEvent event);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(UploadEvent event);
    }

    public HomeAdapter(Context context, ArrayList<UploadEvent> eventList,
                       OnEventClickListener onEventClickListener,
                       OnDeleteClickListener onDeleteClickListener,
                       boolean isAdmin) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = onEventClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.completeevent, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        UploadEvent event = eventList.get(position);

        // completeevent.xml'dagi ID'larga moslashtirilgan
        holder.eventTypeC.setText(event.getEventType() != null ? event.getEventType() : "Noma'lum tadbir");
        holder.venueEventU.setText(event.getEventVenue() != null ? event.getEventVenue() : "Manzil kiritilmagan");
        holder.priceEvent.setText(event.getBudget() != null ? event.getBudget() : "Byudjet kiritilmagan");
        holder.designEvent.setText(event.getDesign() != null ? event.getDesign() : "Dizayn kiritilmagan");

        if (event.getUploadImage() != null && !event.getUploadImage().isEmpty()) {
            Glide.with(context).load(event.getUploadImage())
                    .placeholder(R.drawable.birthday)
                    .error(R.drawable.birthday)
                    .into(holder.eventImg);
        } else {
            holder.eventImg.setImageResource(R.drawable.birthday);
        }

        holder.deleteButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (isAdmin) {
            holder.deleteButton.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(event);
                }
            });
        } else {
            holder.deleteButton.setOnClickListener(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateList(ArrayList<UploadEvent> newList) {
        eventList.clear();
        eventList.addAll(newList);
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTypeC, venueEventU, priceEvent, designEvent;
        ImageView eventImg;
        Button deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            // completeevent.xml'dagi ID'larga moslashtirilgan
            eventTypeC = itemView.findViewById(R.id.eventTypeC);
            venueEventU = itemView.findViewById(R.id.venueEventU);
            priceEvent = itemView.findViewById(R.id.priceEvent);
            designEvent = itemView.findViewById(R.id.designEvent);
            eventImg = itemView.findViewById(R.id.eventImg);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}