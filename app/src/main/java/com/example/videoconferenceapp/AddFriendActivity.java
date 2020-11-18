package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.videoconferenceapp.adapters.ContactAdapter;
import com.example.videoconferenceapp.adapters.DividerItemDecorator;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.User;
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

public class AddFriendActivity extends AppCompatActivity{ //implements SearchView.OnQueryTextListener{

    private DatabaseReference usersRef, notificationsRef;
    private FirebaseAuth firebaseAuth;
    private ArrayList<User> contacts;
    private ArrayList<String> keys;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private String currentUserId;
    private ContactAdapter contactAdapter;
    private List<String> friendsWith;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        setContentView(R.layout.activity_add_friend);

        contacts = new ArrayList<User>();
        keys = new ArrayList<String>();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> childern = dataSnapshot.getChildren();
                usersRef.child(currentUserId).child("friendsWith").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendsWith = (ArrayList<String>) dataSnapshot.getValue();
                        for (DataSnapshot child : childern) {
                            boolean inList = false;
                            User user = child.getValue(User.class);
                            if(!child.getKey().equals(currentUserId)){
                                if(friendsWith == null){
                                    contacts.add(user);
                                    keys.add(child.getKey());
                                }else{
                                    /*for (String friendKey : friendsWith) {
                                        Log.d("FRIEND_KEY", friendKey);
                                        if(!friendKey.equals("lalalala") && friendKey.equals(child.getKey())){
                                            inList = true;
                                        }
                                    }*/
                                    if(friendsWith.contains(child.getKey())){
                                        inList = true;
                                    }
                                }
                                if(!inList && friendsWith != null){
                                    contacts.add(user);
                                    keys.add(child.getKey());
                                }
                            }
                        }
                        init();
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

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_add_friends);
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
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_more:
                        startActivity(new Intent(getApplicationContext(), MoreActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        setBadgeForNotifications(bottomNavigationView,notificationsRef);
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
                    View badge = LayoutInflater.from(AddFriendActivity.this).inflate(R.layout.custom_badge_layout, itemView, true);
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
        contactAdapter = new ContactAdapter(this, contacts, keys,"addFriends");
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(AddFriendActivity.this, R.drawable.recyclerview_driver));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(contactAdapter);
    }

    /*@Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase().trim();
        contactAdapter.filter(newText);
        return false;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
