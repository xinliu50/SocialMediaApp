package com.example.letchattutorial;

public class FindFriends {
    public String profileImage, fullname, status;

    public FindFriends(String profileImage, String fullname, String status) {
        this.profileImage = profileImage;
        this.fullname = fullname;
        this.status = status;
    }

    public FindFriends(){

    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
