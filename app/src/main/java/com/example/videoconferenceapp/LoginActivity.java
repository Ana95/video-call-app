package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoconferenceapp.model.Constant;
import com.example.videoconferenceapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private Button signup_btn, login_btn, reset_password;
    private ImageView imageView;
    private TextView textView;
    private TextInputLayout email,password;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userReference;
    private Boolean userExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.theme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        userExist = false;

        imageView = findViewById(R.id.logo_image);
        textView = findViewById(R.id.logo_text);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signup_btn = findViewById(R.id.signup_btn);
        login_btn = findViewById(R.id.login_btn);
        login_btn.setBackground(getResources().getDrawable(Constant.color));
        reset_password = findViewById(R.id.reset_password);
        firebaseAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);

                Pair[] pairs = new Pair[6];
                pairs[0] = new Pair<View, String>(imageView, "logo_image");
                pairs[1] = new Pair<View, String>(textView, "logo_text");
                pairs[2] = new Pair<View, String>(email, "email_tran");
                pairs[3] = new Pair<View, String>(password, "password_tran");
                pairs[4] = new Pair<View, String>(login_btn, "button_tran");
                pairs[5] = new Pair<View, String>(signup_btn, "login_signup_tran");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResestPasswordDialog();
            }
        });

    }

    private Boolean validateEmail(){
        String val = email.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(val.isEmpty()){
            email.setError("Field cannot be empty!");
            return false;
        }else if(!val.matches(emailPattern)){
            email.setError("Invalid email address");
            return false;
        }
        else {
            email.setError(null);
            return true;
        }

    }

    private Boolean validatePassword(){
        String val = password.getEditText().getText().toString();
        String passwordVal = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";

        if(val.isEmpty()){
            password.setError("Field cannot be empty!");
            return false;
        }else if(!val.matches(passwordVal)){
            password.setError("Password is too weak!");
            return false;
        }else{
            password.setError(null);
            return true;
        }

    }

    public void login(View view) {
        if(validateEmail() | validatePassword()){
            String entered_email = email.getEditText().getText().toString();
            String entered_password = password.getEditText().getText().toString();

            firebaseAuth.signInWithEmailAndPassword(entered_email, entered_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //Toast.makeText(LoginActivity.this, "Successfully Sign In!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, CallsActivity.class));
                    }else{
                        showErrorDialog();
                    }
                }
            });
        }
    }

    private void showErrorDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);

        View dialogView = LayoutInflater.from(LoginActivity.this).inflate(
                R.layout.error_dialog_design,
                (ConstraintLayout)findViewById(R.id.error_dialog));
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

    private void showResestPasswordDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);

        View dialogView = LayoutInflater.from(LoginActivity.this).inflate(
                R.layout.reset_password_dialog,
                (LinearLayout)findViewById(R.id.update_dialog));
        dialogBuilder.setView(dialogView);

        TextInputLayout email = dialogView.findViewById(R.id.email);
        Button resetPasswordBtn = dialogView.findViewById(R.id.reset_password_btn);
        TextView link = dialogView.findViewById(R.id.link);

        AlertDialog alertDialog = dialogBuilder.create();
        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getEditText().getText().toString();
                checkIsUserExist(userEmail, email, link, alertDialog);
            }
        });
    }

    private void checkIsUserExist(String email, TextInputLayout emailInput, TextView link, AlertDialog alertDialog){
        userExist = false;
        Log.d("Before call: " , String.valueOf(userExist));
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> childern = dataSnapshot.getChildren();
                for (DataSnapshot value : childern) {
                    String existingEmail = (String) value.child("email").getValue();
                    if(existingEmail.equals(email)){
                        userExist = true;
                        Log.d("In call: ", String.valueOf(userExist));
                        break;
                    }
                }
                Log.d("userExist", String.valueOf(userExist));
                if(email.equals("")){
                    emailInput.setError("This field is required!");
                }else if(!userExist){
                    emailInput.setError("User with this email doesn't exist!");
                }else{
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                emailInput.setError(null);
                                link.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(() -> {
                                    alertDialog.dismiss();
                                }, 1000);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
