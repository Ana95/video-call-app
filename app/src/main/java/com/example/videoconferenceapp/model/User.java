package com.example.videoconferenceapp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private String userId, name, username, email, phone, password;
    private List<String> friendsWith;

    public User(String name, String username, String email, String phone, String password, List<String> friendsWith){
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.friendsWith = friendsWith;
    }

    public User(){

    }

    public String getUserId(){
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getFriendsWith() {
        return friendsWith;
    }

    public void setFriendsWith(List<String> friendsWith) {
        this.friendsWith = friendsWith;
    }

}
