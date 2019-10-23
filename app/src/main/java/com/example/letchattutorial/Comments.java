package com.example.letchattutorial;

public class Comments {
    public String username, time, date, comment;

    public Comments(){}

    public Comments(String username, String time, String date, String comment) {
        this.username = username;
        this.time = time;
        this.date = date;
        this.comment = comment;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }
}
