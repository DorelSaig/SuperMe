package com.DorelSaig.superme.Objects;

import java.util.ArrayList;
import java.util.LinkedList;

public class MyUser {
    private String uid;
    private String name;
    private String phoneNumber;
    private String profileImgUrl = "https://firebasestorage.googleapis.com/v0/b/superme-e69d5.appspot.com/o/images%2Fimg_profile_pic.JPG?alt=media&token=5970cec0-9663-4ddd-9395-ef2791ad938d";
    private ArrayList<String> myListsUids;



    public MyUser() { }

    public MyUser(String uid, String name, String phoneNumber) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImgUrl = "";
        this.myListsUids = new ArrayList<>();

        }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public MyUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public MyUser setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public MyUser setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
        return this;
    }

    public ArrayList<String> getMyListsUids() {
        return myListsUids;
    }

    public MyUser setMyListsUids(ArrayList<String> myListsUids) {
        this.myListsUids = myListsUids;
        return this;
    }

    @Override
    public String toString() {
        return "MyUser{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profileImgUrl='" + profileImgUrl + '\'' +
                '}';
    }

    public boolean addToListUid(String uidToAdd){
        return this.myListsUids.add(uidToAdd);
    }

    public boolean removeFromListsUids(String listUid) {
        return this.myListsUids.remove(listUid);
    }
}
