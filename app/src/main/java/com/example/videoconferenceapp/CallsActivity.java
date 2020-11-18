package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.videoconferenceapp.adapters.ContactAdapter;
import com.example.videoconferenceapp.adapters.DividerItemDecorator;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.Request;
import com.example.videoconferenceapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CallsActivity extends AppCompatActivity{ //implements SearchView.OnQueryTextListener{

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference friendsRef, usersRef, notificationsRef;
    private FirebaseAuth mAuth;
    private ArrayList<User> contacts;
    private ArrayList<String> keys;
    private String currentUserId;
    private SearchView search;
    private ContactAdapter contactAdapter;
    private Boolean callEnded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        setContentView(R.layout.activity_calls);

        contacts = new ArrayList<User>();
        keys = new ArrayList<String>();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference("Friend Requests");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> childern = dataSnapshot.getChildren();
                for (DataSnapshot child : childern) {
                    Request request = child.getValue(Request.class);
                        if(request.getRequestType().equals("request_accepted")){
                            if(request.getSender().getUserId().equals(currentUserId)){
                                contacts.add(request.getReceiver());
                                keys.add(request.getReceiver().getUserId());
                            }
                            if(request.getReceiver().getUserId().equals(currentUserId)){
                                contacts.add(request.getSender());
                                keys.add(request.getSender().getUserId());
                            }
                        }
                }
                init();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_calls);
        bottomNavigationView.setItemBackground(getResources().getDrawable(Constant.color));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.navigation_calls:
                        return true;
                    case R.id.navigation_add_friends:
                        startActivity(new Intent(getApplicationContext(), AddFriendActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_more:
                        startActivity(new Intent(getApplicationContext(), MoreActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;
            }
        });

        setBadgeForNotifications(bottomNavigationView, notificationsRef);

    }

    private void setBadgeForNotifications(BottomNavigationView bottomNavigationView, DatabaseReference notificationsRef){
        notificationsRef.child(currentUserId).child("numOfNotifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long numOfNotifications = (Long) dataSnapshot.getValue();
                if(numOfNotifications != null){
                    BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
                    View v = bottomNavigationMenuView.getChildAt(2);
                    BottomNavigationItemView itemView = (BottomNavigationItemView) v;
                    View badge = LayoutInflater.from(CallsActivity.this).inflate(R.layout.custom_badge_layout, itemView, true);
                    TextView counter = badge.findViewById(R.id.notifications_badge);
                    counter.setText(String.valueOf(numOfNotifications));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(CallsActivity.this, contacts, keys,"calls");
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(CallsActivity.this, R.drawable.recyclerview_driver));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(contactAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUserId = mAuth.getCurrentUser().getUid();
        callEnded = Boolean.valueOf(getIntent().getStringExtra("callEnded"));
        if(callEnded == null){
            callEnded = false;
        }
        manageActivites();
    }

    private void checkForReceivingCall(){
        usersRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ringing")){
                    Log.d("checkForReceivingCall", "have Ringing");
                    String calledBy = dataSnapshot.child("ringing").getValue().toString();
                    usersRef.child(calledBy).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            Intent intent = new Intent(CallsActivity.this, CallingActivity.class);
                            intent.putExtra("key", calledBy);
                            intent.putExtra("contactName", user.getName());
                            startActivity(intent);
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

    private void cancelCallingUser(){
        Log.d("CallingActivity", " pocetak metode za brisanje");
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Calling").exists()){
                    String callingID = dataSnapshot.child("Calling").child("calling").getValue().toString();
                    Log.d("CallingActivity", " have calling attribute");
                    usersRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                usersRef.child(currentUserId).child("Calling").removeValue();
                            }
                        }
                    });
                }else{
                    if(dataSnapshot.child("Ringing").exists()){
                        String ringingID = dataSnapshot.child("Ringing").child("ringing").getValue().toString();

                        usersRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    usersRef.child(currentUserId).child("Ringing").removeValue();
                                }
                            }
                        });
                    }
                }
                callEnded = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageActivites(){
        if(callEnded){
            cancelCallingUser();
        }else{
            checkForReceivingCall();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
