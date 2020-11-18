package com.example.videoconferenceapp.model;

public class Request {

    private User sender;

    private String requestType;

    private User receiver;

    private String visibility;

    public Request() {
    }

    public Request(User sender, String requestType, User receiver, String visibility) {
        this.sender = sender;
        this.requestType = requestType;
        this.receiver = receiver;
        this.visibility = visibility;
    }

    public User getSender() {
        return sender;
    }

    public String getRequestType() {
        return requestType;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

}
