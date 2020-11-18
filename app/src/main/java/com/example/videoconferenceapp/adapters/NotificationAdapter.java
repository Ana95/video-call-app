package com.example.videoconferenceapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoconferenceapp.R;
import com.example.videoconferenceapp.helperFunctions.ImagesHelperFunctions;
import com.example.videoconferenceapp.model.Request;
import com.example.videoconferenceapp.viewHolders.ContactViewHolder;
import com.example.videoconferenceapp.viewHolders.NotificationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    private Context mContext;
    private ArrayList<Request> requests;
    private ArrayList<String> requestKeys;
    private ArrayList<Request> requestsFullList;
    private ArrayList<String> keysFullList;
    private DatabaseReference friendRequestsRef;
    private DatabaseReference usersReference;
    private FirebaseAuth firebaseAuth;
    private Set<String> friendsKey;

    public NotificationAdapter(Context mContext, ArrayList<Request> requests, ArrayList<String> requestKeys){
        this.mContext = mContext;
        this.requests = requests;
        this.requestKeys = requestKeys;
        requestsFullList = new ArrayList<>(requests);
        keysFullList = new ArrayList<>(requestKeys);
        friendRequestsRef = FirebaseDatabase.getInstance().getReference("Friend Requests");
        usersReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        friendsKey = new HashSet<String>();
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ImagesHelperFunctions.loadImage(holder.contactImg, requests.get(position).getSender().getUserId(), mContext);
        holder.contactName.setText(requests.get(position).getSender().getName());
        manageNotifications(requestKeys.get(position), holder);
        //manageCloseNotification(requestKeys.get(position), "gone", position);
        holder.contactPhone.setText(requests.get(position).getSender().getPhone());
        holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriendRequest(requestKeys.get(position), "request_accepted", holder, position);
            }
        });
        holder.deleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest(requestKeys.get(position), position);
            }
        });
        holder.closeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeNotification(requestKeys.get(position), "gone", position);
            }
        });
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
        NotificationViewHolder nvh = new NotificationViewHolder(v);
        return  nvh;
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    private void acceptFriendRequest(String requestKey, String requestType, NotificationViewHolder holder, int position){
        friendRequestsRef.child(requestKey).child("requestType").setValue(requestType);
        friendRequestsRef.child(requestKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Request request = dataSnapshot.getValue(Request.class);
                String currentUser = firebaseAuth.getCurrentUser().getUid();
                if(request.getRequestType().equals(requestType)){
                    requests.set(position, request);
                    notifyChangesHappen(position);
                    usersReference.child(request.getSender().getUserId()).child("friendsWith").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<String> friendsWith = new ArrayList<String >();
                            if(dataSnapshot.getValue() != null){
                                friendsWith = (ArrayList<String>) dataSnapshot.getValue();
                            }
                            friendsWith.add(request.getReceiver().getUserId());
                            usersReference.child(request.getSender().getUserId()).child("friendsWith").setValue(friendsWith);
                            usersReference.child(request.getReceiver().getUserId()).child("friendsWith").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    List<String> friends_with = new ArrayList<String >();
                                    if(dataSnapshot.getValue() != null){
                                        friends_with = (ArrayList<String>) dataSnapshot.getValue();
                                    }
                                    friends_with.add(request.getSender().getUserId());
                                    usersReference.child(request.getReceiver().getUserId()).child("friendsWith").setValue(friends_with);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void manageNotifications(String requestKey, NotificationViewHolder holder){
        friendRequestsRef.child(requestKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Request request = dataSnapshot.getValue(Request.class);
                if(request != null){
                    if(request.getRequestType().equals("request_accepted")){
                        holder.acceptRequest.setVisibility(View.GONE);
                        holder.deleteRequest.setVisibility(View.GONE);
                        holder.frinedsBtn.setVisibility(View.VISIBLE);
                    }else if(request.getRequestType().equals("request_sent")){
                        holder.acceptRequest.setVisibility(View.VISIBLE);
                        holder.deleteRequest.setVisibility(View.VISIBLE);
                        holder.frinedsBtn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void notifyChangesHappen(int position){
        this.notifyItemChanged(position, requests.size());
    }

    private void deleteRequest(String requestKey, int position){
        friendRequestsRef.child(requestKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    requests.remove(position);
                    requestKeys.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, requestKeys.size());
                }
            }
        });
    }

    private void closeNotification(String requestKey, String visibility, int position){
        friendRequestsRef.child(requestKey).child("visibility").setValue(visibility);
        requests.remove(position);
        requestKeys.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, requestKeys.size());

    }

    private void manageCloseNotification(String requestKey, String visibility, int position){
        friendRequestsRef.child(requestKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Request request = dataSnapshot.getValue(Request.class);
                if(request.getVisibility().equals(visibility)){
                    requests.remove(position);
                    //notifyDataDeleted(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        requests.clear();
        requestKeys.clear();
        if (charText.length() == 0) {
            requests.addAll(requestsFullList);
            requestKeys.addAll(keysFullList);
        } else {
            for(int i = 0; i < requestsFullList.size(); i++){
                if(requestsFullList.get(i).getSender().getName().toLowerCase().contains(charText)){
                    requests.add(requestsFullList.get(i));
                    requestKeys.add(keysFullList.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }*/


}