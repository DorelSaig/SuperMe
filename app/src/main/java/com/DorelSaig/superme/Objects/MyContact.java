package com.DorelSaig.superme.Objects;

public class MyContact {
    private String uid;
    private String name;
    private String phone;
    private String imgUrl = "https://firebasestorage.googleapis.com/v0/b/superme-e69d5.appspot.com/o/images%2Fimg_profile_pic.JPG?alt=media&token=5970cec0-9663-4ddd-9395-ef2791ad938d";

    public MyContact() {
    }

    public MyContact(String uid, String name, String phone, String imgUrl) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.imgUrl = imgUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public MyContact setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public MyContact setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public MyContact setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }
}
