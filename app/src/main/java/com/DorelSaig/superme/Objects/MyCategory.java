package com.DorelSaig.superme.Objects;

import java.util.UUID;

public class MyCategory {
    String catUid;
    String title = "";
    String cat_cover = "https://firebasestorage.googleapis.com/v0/b/superme-e69d5.appspot.com/o/images%2Fimg_default_list_cover.jpg?alt=media&token=60b8db0a-91fd-4a10-9bc3-47418f158da1";

    public MyCategory() {
    }

    public MyCategory(String title) {
        this.catUid = UUID.randomUUID().toString();
        this.title = title;
    }

    public String getCatUid() {
        return catUid;
    }

    public String getTitle() {
        return title;
    }

    public MyCategory setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCat_cover() {
        return cat_cover;
    }

    public MyCategory setCat_cover(String cat_cover) {
        this.cat_cover = cat_cover;
        return this;
    }
}
