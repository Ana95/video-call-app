package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconferenceapp.adapters.OptionsAdapter;
import com.example.videoconferenceapp.helperFunctions.ImagesHelperFunctions;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MoreActivity extends AppCompatActivity {

    private ListView options;
    private int[] images = {R.drawable.ic_settings_applications_black_24dp, R.drawable.ic_help_black_24dp, R.drawable.ic_directions_black_24dp};
    private String[] names = new String[3];
    private BottomNavigationView bottomNavigationView;
    private ImageView imageView;
    private TextView name, phone;
    private FirebaseAuth mAuth;
    private StorageReference firebaseStorage;
    private User currentUser;
    private DatabaseReference usersRef, notificationsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        setContentView(R.layout.activity_more);

        names[0] = this.getString(R.string.settings);
        names[1] = this.getString(R.string.help);
        names[2] = this.getString(R.string.log_out);

        imageView = findViewById(R.id.image);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        mAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance().getReference("userImages");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        currentUserId = mAuth.getCurrentUser().getUid();
        firebaseStorage.child(currentUserId + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImagesHelperFunctions.loadImageToImageView(uri, imageView);
                usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);
                        name.setText(currentUser.getName());
                        phone.setText(currentUser.getPhone());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MoreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, ProfileActivity.class));
            }
        });

        init();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_more);
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
                        startActivity(new Intent(getApplicationContext(), NotificationsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.navigation_more:
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
                    View badge = LayoutInflater.from(MoreActivity.this).inflate(R.layout.custom_badge_layout, itemView, true);
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
        options = findViewById(R.id.options);
        OptionsAdapter optionsAdapter = new OptionsAdapter(this, images, names);
        options.setAdapter(optionsAdapter);
        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(names[position].equals(MoreActivity.this.getString(R.string.settings))){
                    startActivity(new Intent(MoreActivity.this, SettingsActivity.class));
                }else if(names[position].equals(MoreActivity.this.getString(R.string.log_out))){
                    mAuth.signOut();
                    startActivity(new Intent(MoreActivity.this, LoginActivity.class));
                }else{
                    startActivity(new Intent(MoreActivity.this, HelpActivity.class));
                }
            }
        });
    }

}
