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
import java.util.function.Consumer;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private ArrayList<UploadEvent> eventList;
    private Context context;
    private Consumer<UploadEvent> onEventClickListener;
    private Consumer<UploadEvent> onDeleteClickListener;
    private boolean isAdmin;

    public EventAdapter(ArrayList<UploadEvent> eventList, Context context,
                        Consumer<UploadEvent> onEventClickListener,
                        Consumer<UploadEvent> onDeleteClickListener, boolean isAdmin) {
        this.eventList = eventList;
        this.context = context;
        this.onEventClickListener = onEventClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.completeevent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadEvent event = eventList.get(position);

        holder.eventTypeC.setText(event.getEventType() != null ? event.getEventType() : "Noma’lum tadbir");
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
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.accept(event);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.accept(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTypeC, venueEventU, priceEvent, designEvent;
        ImageView eventImg;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTypeC = itemView.findViewById(R.id.eventTypeC);
            venueEventU = itemView.findViewById(R.id.venueEventU);
            priceEvent = itemView.findViewById(R.id.priceEvent);
            designEvent = itemView.findViewById(R.id.designEvent);
            eventImg = itemView.findViewById(R.id.eventImg);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}