package com.example.videoconferenceapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.nfc.Tag;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoconferenceapp.CallingActivity;
import com.example.videoconferenceapp.R;
import com.example.videoconferenceapp.helperFunctions.ImagesHelperFunctions;
import com.example.videoconferenceapp.model.Request;
import com.example.videoconferenceapp.model.User;
import com.example.videoconferenceapp.viewHolders.ContactViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> implements Filterable{

    private ArrayList<User> contacts;
    private ArrayList<String> userKeys;
    private ArrayList<User> contactsFullList;
    private ArrayList<String> keysFullList;
    private Context context;
    private String goal;
    private DatabaseReference friendRequestRef, usersRef;
    private FirebaseAuth mAuth;

    public ContactAdapter(Context context, ArrayList<User> contacts, ArrayList<String> userKeys, String goal){
        this.context = context;
        this.contacts = contacts;
        this.userKeys = userKeys;
        contactsFullList = new ArrayList<>(contacts);
        keysFullList = new ArrayList<>(userKeys);
        this.goal = goal;
        mAuth = FirebaseAuth.getInstance();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
        ContactViewHolder cvh = new ContactViewHolder(v);
        return  cvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        bindManage(goal, holder, position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void bindManage(String goal, ContactViewHolder holder, int position){
        if(goal.equals("calls")){
            callBinding(holder, position);
        }else if(goal.equals("addFriends")){
            addFriendsBinding(holder, position);
        }
    }

    private void callBinding(ContactViewHolder holder, int position){
        ImagesHelperFunctions.loadImage(holder.contactImg, userKeys.get(position), context);
        holder.contactName.setText(contacts.get(position).getName());
        holder.contactPhone.setText(contacts.get(position).getPhone());
        holder.videoCallImg.setVisibility(View.VISIBLE);
        holder.notificationLayout.setVisibility(View.GONE);

        holder.videoCallImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CallingActivity.class);
                intent.putExtra("key", userKeys.get(position));
                intent.putExtra("contactName", contacts.get(position).getName());
                intent.putExtra("makeCall", true);
                context.startActivity(intent);

            }
        });
    }

    private void addFriendsBinding(ContactViewHolder holder, int position) {
        ImagesHelperFunctions.loadImage(holder.contactImg, userKeys.get(position), context);
        holder.contactName.setText(contacts.get(position).getName());
        holder.contactPhone.setText(contacts.get(position).getPhone());
        holder.notificationLayout.setVisibility(View.GONE);
        holder.addFriendImg.setVisibility(View.VISIBLE);
        manageRequests(userKeys.get(position), holder);
        holder.addFriendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFriendRequest(userKeys.get(position), holder);
            }
        });

        holder.cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFriendRequest(userKeys.get(position), holder, position);
            }
        });

    }

    private void sendFriendRequest(String receiverUserId, ContactViewHolder holder){
        String senderUserId = mAuth.getCurrentUser().getUid();

        String requestId = senderUserId + "_" + receiverUserId;

        usersRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User sender = dataSnapshot.getValue(User.class);
                sender.setUserId(senderUserId);
                usersRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User receiver = dataSnapshot.getValue(User.class);
                        receiver.setUserId(receiverUserId);
                        Request request = new Request(sender, "request_sent", receiver, "visible");
                        friendRequestRef.child(requestId).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                holder.cancelRequest.setVisibility(View.VISIBLE);
                                holder.addFriendImg.setVisibility(View.GONE);
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("Notifications");
                                databaseReference.child(request.getReceiver().getUserId()).child("numOfNotifications").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Long numOfNotifications = (Long)  dataSnapshot.getValue();
                                        if(dataSnapshot.getValue() == null){
                                            databaseReference.child(request.getReceiver().getUserId()).child("numOfNotifications").setValue(1);
                                        }else{
                                            numOfNotifications += 1;
                                            databaseReference.child(request.getReceiver().getUserId()).child("numOfNotifications").setValue(numOfNotifications);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void cancelFriendRequest(String recieverUserId, ContactViewHolder holder, int position){
        String senderUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef.child(senderUserId + "_" + recieverUserId).child("requestType").setValue("request_withdrawn");
        holder.cancelRequest.setVisibility(View.GONE);
        holder.addFriendImg.setVisibility(View.VISIBLE);
        notifyChangesHappen(position);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Notifications");
        databaseReference.child(recieverUserId).child("numOfNotifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Long numOfNotifications = (Long) dataSnapshot.getValue();
                    if(numOfNotifications > 0){
                        numOfNotifications -= 1;
                        if(numOfNotifications == 0){
                            databaseReference.child(recieverUserId).removeValue();
                        }else{
                            databaseReference.child(recieverUserId).child("numOfNotifications").setValue(numOfNotifications);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void manageRequests(String receiverUserId, ContactViewHolder holder){
        String senderUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef.child(senderUserId + "_" + receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Request request = dataSnapshot.getValue(Request.class);
                if(request != null){
                    if(request.getRequestType().equals("request_sent")){
                        holder.addFriendImg.setVisibility(View.GONE);
                        holder.cancelRequest.setVisibility(View.VISIBLE);
                    }else{
                        holder.addFriendImg.setVisibility(View.VISIBLE);
                        holder.cancelRequest.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void notifyDataDeleted(int position){
        this.notifyItemRemoved(position);
    }

    private void notifyChangesHappen(int position){
        this.notifyItemChanged(position, contacts.size());
    }

    public Filter getFilter(){
        return contactFilter;
    }

    private Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredUserList = new ArrayList<User>();
            List<String> filteredUserKeysList = new ArrayList<String>();

            if(constraint == null || constraint.length() == 0){
                filteredUserList.addAll(contactsFullList);
                filteredUserKeysList.addAll(keysFullList);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(int i = 0; i < contactsFullList.size(); i++){
                    if(contactsFullList.get(i).getName().toLowerCase().contains(filterPattern)){
                        filteredUserList.add(contactsFullList.get(i));
                        filteredUserKeysList.add(keysFullList.get(i));
                    }
                }
            }

            FilterResults results = new FilterResults();

            HashMap<String, List> data = new HashMap<>();
            data.put("users", filteredUserList);
            data.put("keys", filteredUserKeysList);
            results.values = data;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contacts.clear();
            userKeys.clear();
            HashMap<String, List> data = (HashMap<String, List>) results.values;
            contacts.addAll(data.get("users"));
            userKeys.addAll(data.get("keys"));
            notifyDataSetChanged();
        }
    };

}