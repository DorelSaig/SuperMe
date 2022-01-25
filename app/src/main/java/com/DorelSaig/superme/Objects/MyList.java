package com.DorelSaig.superme.Objects;

import java.util.ArrayList;
import java.util.UUID;

public class MyList {

    private String listUid;
    private String title = "";
    private int items_Counter = 0;
    private String image_cover = "https://firebasestorage.googleapis.com/v0/b/superme-e69d5.appspot.com/o/images%2Fimg_default_list_cover.jpg?alt=media&token=60b8db0a-91fd-4a10-9bc3-47418f158da1";
    private ArrayList<String> itemsUid;
    private String creatorUid = "";
    private ArrayList<String> sharedWithUidsList;

    public MyList() {
    }

    public MyList(String title, String creatorUid) {
        this.listUid = UUID.randomUUID().toString();
        this.title = title;
        this.image_cover = image_cover; //Todo put default cover
        this.creatorUid = creatorUid;
        this.itemsUid = new ArrayList<>();
        this.sharedWithUidsList = new ArrayList<>();
    }

    public String getListUid() {
        return listUid;
    }

    public String getTitle() {
        return title;
    }

    public MyList setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getItems_Counter() {
        return items_Counter;
    }

    public MyList setItems_Counter(int items_Counter) {
        this.items_Counter = items_Counter;
        return this;
    }

    public String getImage_cover() {
        return image_cover;
    }

    public MyList setImage_cover(String image_cover) {
        this.image_cover = image_cover;
        return this;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public ArrayList<String> getItemsUid() {
        return itemsUid;
    }
}
