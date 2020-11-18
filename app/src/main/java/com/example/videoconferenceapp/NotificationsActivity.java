package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.videoconferenceapp.adapters.ContactAdapter;
import com.example.videoconferenceapp.adapters.DividerItemDecorator;
import com.example.videoconferenceapp.adapters.NotificationAdapter;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.Request;
import com.example.videoconferenceapp.model.User;
import com.example.videoconferenceapp.viewHolders.NotificationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity{ //implements SearchView.OnQueryTextListener{

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private DatabaseReference friendRequestsRef, notificationsRef;
    private FirebaseAuth mAuth;
    private ArrayList<Request> requests;
    private ArrayList<String> requestKeys;
    private NotificationAdapter notificationAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        setContentView(R.layout.activity_notifications);

        requests = new ArrayList<Request>();
        requestKeys = new ArrayList<String>();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        friendRequestsRef = FirebaseDatabase.getInstance().getReference("Friend Requests");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        friendRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> childern = dataSnapshot.getChildren();
                for (DataSnapshot data : childern) {
                    Request request = data.getValue(Request.class);
                    if(request.getReceiver().getUserId().equals(currentUserId) && request.getVisibility().equals("visible") &&
                        !request.getRequestType().equals("request_withdrawn")){
                        requests.add(request);
                        requestKeys.add(data.getKey());
                    }
                }
                init();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_notifications);
        bottomNavigationView.setItemBackground(getResources().getDrawable(Constant.color));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navigation_calls:
                        startActivity(new Intent(getApplicationContext(), CallsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_add_friends:
                        startActivity(new Intent(getApplicationContext(), AddFriendActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_notifications:
                        return true;
                    case R.id.navigation_more:
                        startActivity(new Intent(getApplicationContext(), MoreActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        removeBadgeForNotifications(notificationsRef);

    }

    private void removeBadgeForNotifications(DatabaseReference notificationsRef){
        notificationsRef.child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("STATUS", "Successfully removed!");
            }
        });
    }

    public void init(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(this, requests, requestKeys);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(NotificationsActivity.this, R.drawable.recyclerview_driver));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(notificationAdapter);
    }

}
