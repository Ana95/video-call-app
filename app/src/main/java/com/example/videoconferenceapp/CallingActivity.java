package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconferenceapp.helperFunctions.ImagesHelperFunctions;
import com.example.videoconferenceapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private ImageView callingImg, makeCallBtn, cancelCallBtn;
    private TextView contactName;
    private StorageReference firebaseStorage;
    private String currentUserId, receiverUserId, receiverUserName;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private boolean haveCallButton;

    //private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        callingImg = findViewById(R.id.calling_img);
        makeCallBtn = findViewById(R.id.make_call);
        cancelCallBtn = findViewById(R.id.cancel_call);
        contactName = findViewById(R.id.name_calling);

        //mediaPlayer = MediaPlayer.create(this, R.raw.ringing);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseStorage = FirebaseStorage.getInstance().getReference("userImages");

        receiverUserId = getIntent().getStringExtra("key");
        receiverUserName = getIntent().getStringExtra("contactName");
        haveCallButton = Boolean.valueOf(getIntent().getStringExtra("haveCallButton"));
        currentUserId = mAuth.getCurrentUser().getUid();

        firebaseStorage.child(receiverUserId + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImagesHelperFunctions.loadImageToImageView(uri, callingImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CallingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        contactName.setText(receiverUserName);
        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mediaPlayer.stop();
                cancelCallingUser();
            }
        });

        makeCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mediaPlayer.stop();
                HashMap<String, Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked", "picked");

                usersRef.child(currentUserId).child("Ringing").updateChildren(callingPickUpMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUserId = mAuth.getCurrentUser().getUid();
        storageData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void storageData(){
        //mediaPlayer.start();
        usersRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("Ringing") & !dataSnapshot.hasChild("Calling")){
                    usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.hasChild("Ringing") & !dataSnapshot.hasChild("Calling")){
                                HashMap<String, Object> ringingInfo = new HashMap<>();
                                ringingInfo.put("ringing", currentUserId);
                                usersRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);

                                HashMap<String, Object> callingInfo = new HashMap<>();
                                callingInfo.put("calling", receiverUserId);
                                usersRef.child(currentUserId).child("Calling").updateChildren(callingInfo);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                manageVisibilityOfButtons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageVisibilityOfButtons(){
        Log.d("Manage_visibility", "method");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(currentUserId).hasChild("Ringing") & !dataSnapshot.child(currentUserId).hasChild("Calling")){
                    makeCallBtn.setVisibility(View.VISIBLE);
                }

                if(dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")){
                    //mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void cancelCallingUser(){
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Calling").exists()){
                    String callingID = dataSnapshot.child("Calling").child("calling").getValue().toString();

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
                startActivity(new Intent(CallingActivity.this, CallsActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ON_STOP/calling activity", " method");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ON_DESTROY/calling activity", " method");
    }
}
