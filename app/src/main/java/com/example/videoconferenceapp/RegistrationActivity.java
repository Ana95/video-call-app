package com.example.videoconferenceapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.videoconferenceapp.helperFunctions.FieldValidation;
import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputLayout regName, regUsername, regEmail, regPhone, regPassword;
    private SwitchCompat switchCompat;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference userImagesRef;
    private Boolean isFemale = false;
    private Button regBtn, alreadyRegistered;
    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        regName = findViewById(R.id.reg_name);
        regUsername = findViewById(R.id.reg_username);
        regEmail = findViewById(R.id.reg_email);
        regPhone = findViewById(R.id.reg_phone);
        regPassword = findViewById(R.id.reg_password);
        regBtn = findViewById(R.id.reg_btn);
        alreadyRegistered = findViewById(R.id.already_registered);
        regBtn.setBackground(getResources().getDrawable(Constant.color));
        switchCompat = findViewById(R.id.gender);
        setSwitchCompatColor(switchCompat, Constant.color);
        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchCompat.isChecked()){
                    isFemale = true;
                }else{
                    isFemale = false;
                }
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(v);
            }
        });
        alreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goBack = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(goBack);
            }
        });
    }
    private void setSwitchCompatColor(SwitchCompat switchCompat, int color){
        int thumbColor = Color.parseColor(getResources().getString(color));
        int trackColor = Color.argb(77, Color.red(thumbColor), Color.green(thumbColor), Color.blue(thumbColor));

        DrawableCompat.setTintList(switchCompat.getThumbDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        thumbColor,
                        Color.WHITE
                }));

        DrawableCompat.setTintList(switchCompat.getTrackDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        trackColor,
                        Color.parseColor("#4D000000")
                }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String perms[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this, perms)){
            Toast.makeText(this, "Permission GRANTED!", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Permission DENIED!", Toast.LENGTH_LONG).show();
        }
    }

    public void registerUser(View view) {
        if(FieldValidation.validateName(regName) & FieldValidation.validateUsername(regUsername)
                & FieldValidation.validateEmail(regEmail) & FieldValidation.validatePhone(regPhone) & FieldValidation.validatePassword(regPassword) &
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Users");
            userImagesRef = FirebaseStorage.getInstance().getReference("userImages");

            String name = regName.getEditText().getText().toString();
            String username = regUsername.getEditText().getText().toString();
            String email = regEmail.getEditText().getText().toString();
            String phone = regPhone.getEditText().getText().toString();
            String password = regPassword.getEditText().getText().toString();

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        User user = new User(name, username, email, phone, password, new ArrayList<String>());
                        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(user);
                        uploadUserImage(userImagesRef, firebaseAuth.getCurrentUser().getUid());
                        showSuccessDialog();
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                        }, 2000);

                    }else{
                        Toast.makeText(RegistrationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            requestPermissions();
        }
    }

    private void uploadUserImage(StorageReference imageRef, String currentUserKey) {
        File file = createTargetFile("drawable", "defaultImage.png", imageToBitmap(RegistrationActivity.this));
        Log.d("FILE", getImageUri("drawable", "defaultImage.png").toString());

        imageRef.child(currentUserKey + ".png").putFile(getImageUri("drawable", "defaultImage.png")).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegistrationActivity.this, "Upload successful!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestPermissions(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) &
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permissions needed!")
                    .setMessage("This permissions are needed because of users default profile picture upload!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(RegistrationActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private Uri getImageUri(String packageName, String imageName) {
        Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + packageName, imageName));
        return imageUri;
    }

    private File createTargetFile(String directoryName, String imageName, Bitmap imageBitmap){
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + directoryName);
        Boolean doSave = true;
        if (!dir.exists()) {
            doSave = dir.mkdirs();
        }

        if (doSave) {
            saveBitmapToFile(dir, imageName, imageBitmap, Bitmap.CompressFormat.PNG,100);
        }
        else {
            Log.e("app","Couldn't create target directory.");
        }
        return dir;
    }

    private Boolean saveBitmapToFile(File dir, String fileName, Bitmap bm, Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format,quality,fos);
            fos.close();
            return true;
        }
        catch (IOException e) {
            Log.e("app", e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    private Bitmap imageToBitmap(Context mContext){
        Bitmap bitmap = null;
        if(isFemale){
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_profile_woman);
        }else{
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_profile_man);
        }
        return bitmap;
    }

    private void showSuccessDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RegistrationActivity.this, R.style.AlertDialogTheme);

        View dialogView = LayoutInflater.from(RegistrationActivity.this).inflate(
                R.layout.success_dialog_design,
                (ConstraintLayout)findViewById(R.id.success_dialog));
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        Button okBtn = dialogView.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

}
