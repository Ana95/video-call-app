package com.example.videoconferenceapp.helperFunctions;

import com.google.android.material.textfield.TextInputLayout;

public class FieldValidation {

    public static Boolean validateName(TextInputLayout name_input){
        String val = name_input.getEditText().getText().toString();

        if(val.isEmpty()){
            name_input.setError("Field cannot be empty!");
            return false;
        }else{
            name_input.setError(null);
            name_input.setErrorEnabled(false);
            return true;
        }

    }

    public static Boolean validateUsername(TextInputLayout username_text){
        String val = username_text.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";

        if(val.isEmpty()){
            username_text.setError("Field cannot be empty!");
            return false;
        }else if(val.length() >= 15){
            username_text.setError("Username too long!");
            return false;
        }else if(!val.matches(noWhiteSpace)){
            username_text.setError("White Spaces are not allowed!");
            return false;
        }else{
            username_text.setError(null);
            username_text.setErrorEnabled(false);
            return true;
        }

    }

    public static Boolean validateEmail(TextInputLayout email_input){
        String val = email_input.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(val.isEmpty()){
            email_input.setError("Field cannot be empty!");
            return false;
        }else if(!val.matches(emailPattern)){
            email_input.setError("Invalid email address");
            return false;
        }
        else {
            email_input.setError(null);
            return true;
        }

    }

    public static Boolean validatePhone(TextInputLayout phone_input){
        String val = phone_input.getEditText().getText().toString();

        if(val.isEmpty()){
            phone_input.setError("Field cannot be empty!");
            return false;
        }else{
            phone_input.setError(null);
            return true;
        }

    }

    public static Boolean validatePassword(TextInputLayout password_input){
        String val = password_input.getEditText().getText().toString();
        String passwordVal = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";

        if(val.isEmpty()){
            password_input.setError("Field cannot be empty!");
            return false;
        }else if(!val.matches(passwordVal)){
            password_input.setError("Password is too weak!");
            return false;
        }else{
            password_input.setError(null);
            return true;
        }

    }

    public static Boolean validateRepeatPassword(TextInputLayout password_input, TextInputLayout repeat_password_input){
        String passwordVal = password_input.getEditText().getText().toString();
        String repeatPasswordVal = repeat_password_input.getEditText().getText().toString();

        if(repeatPasswordVal.isEmpty()){
            repeat_password_input.setError("Field cannot be empty!");
            return false;
        }else if(!passwordVal.equals(repeatPasswordVal)){
            repeat_password_input.setError("Password mismatch!");
            return false;
        }else{
            repeat_password_input.setError(null);
            return true;
        }
    }

}
