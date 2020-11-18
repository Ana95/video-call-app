package com.example.videoconferenceapp.viewHolders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoconferenceapp.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    public ImageView contactImg;
    public TextView contactName;
    public TextView contactPhone;
    public LinearLayout notificationLayout;
    public Button acceptRequest;
    public Button deleteRequest;
    public ImageView closeNotification;
    public Button frinedsBtn;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        contactImg = itemView.findViewById(R.id.img_contact);
        contactName = itemView.findViewById(R.id.contact_name);
        contactPhone = itemView.findViewById(R.id.contact_phone);
        notificationLayout = itemView.findViewById(R.id.notification_layout);
        acceptRequest = itemView.findViewById(R.id.accept_request);
        deleteRequest = itemView.findViewById(R.id.delete_request);
        closeNotification = itemView.findViewById(R.id.close_notification);
        frinedsBtn = itemView.findViewById(R.id.friends_btn);
    }
}
