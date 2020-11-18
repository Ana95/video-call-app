package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconferenceapp.helperFunctions.FieldValidation;
import com.example.videoconferenceapp.helperFunctions.ImagesHelperFunctions;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView fullName, phone, email;
    private RelativeLayout relativeLayout;
    private TextInputLayout profile_username, profile_password, profile_phone, profile_email;
    private Button updateBtn;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private String currentUserId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        relativeLayout = findViewById(R.id.relative_layout);
        relativeLayout.setBackground(getResources().getDrawable(Constant.color));
        fullName = findViewById(R.id.full_name);
        phone = findViewById(R.id.phone_txt);
        email = findViewById(R.id.email_txt);
        profile_username = findViewById(R.id.profile_username);
        profile_password = findViewById(R.id.profile_password);
        profile_phone = findViewById(R.id.profile_phone);
        profile_email = findViewById(R.id.profile_email);
        imageView = findViewById(R.id.profile_image);
        updateBtn = findViewById(R.id.updateBtn);
        updateBtn.setBackground(getResources().getDrawable(Constant.color));

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference("userImages");

        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                ImagesHelperFunctions.loadImage(imageView, currentUserId, ProfileActivity.this);
                fullName.setText(currentUser.getName());
                phone.setText(currentUser.getPhone());
                email.setText(currentUser.getEmail());
                profile_username.getEditText().setText(currentUser.getUsername());
                profile_password.getEditText().setText(currentUser.getPassword());
                profile_phone.getEditText().setText(currentUser.getPhone());
                profile_email.getEditText().setText(currentUser.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateDialog(currentUser);
            }
        });
    }

    public void openFileChooser(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST & resultCode == RESULT_OK & data != null & data.getData() != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);
            uploadFile();
        }
    }

    private void uploadFile(){
        if(imageUri != null){
            StorageReference fileReference = storageReference.child(firebaseAuth.getCurrentUser().getUid() + ".png");

            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ProfileActivity.this, "Upload successful!", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(ProfileActivity.this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDialog(User user){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this, R.style.AlertDialogTheme);

        View dialogView = LayoutInflater.from(ProfileActivity.this).inflate(
                R.layout.update_dialog,
                (LinearLayout)findViewById(R.id.update_dialog));
        dialogBuilder.setView(dialogView);

        TextInputLayout username = dialogView.findViewById(R.id.profile_username);
        TextInputLayout phone = dialogView.findViewById(R.id.profile_phone);
        TextInputLayout email = dialogView.findViewById(R.id.profile_email);
        Button updateBtn = dialogView.findViewById(R.id.updateBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

        username.getEditText().setText(user.getUsername());
        phone.getEditText().setText(user.getPhone());
        email.getEditText().setText(user.getEmail());

        AlertDialog alertDialog = dialogBuilder.create();
        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkIsFieldEmpty(username, phone, email)){
                    alertDialog.dismiss();
                }
            }
        });

    }

    private void updateUser(String username, String phone, String email){
        databaseReference.child(currentUserId).child("username").setValue(username);
        databaseReference.child(currentUserId).child("phone").setValue(phone);
        databaseReference.child(currentUserId).child("email").setValue(email);
        if(!firebaseAuth.getCurrentUser().getEmail().equals(email)){
            FirebaseUser user = firebaseAuth.getCurrentUser();
            user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("EMAIL:", "User email address updated.");
                    }
                }
            });
        }
    }

    private Boolean checkIsFieldEmpty(TextInputLayout username, TextInputLayout phone, TextInputLayout email){
        String usernameValue = username.getEditText().getText().toString();
        String phoneValue = phone.getEditText().getText().toString();
        String emailValue = email.getEditText().getText().toString();

        if(FieldValidation.validateUsername(username) & FieldValidation.validatePhone(phone) & FieldValidation.validateEmail(email)){
            updateUser(usernameValue, phoneValue, emailValue);
            return true;
        }else{
            return false;
        }
    }

}
