package com.DorelSaig.superme.Objects;

import java.util.ArrayList;
import java.util.LinkedList;

public class List {

    private String listUid;
    private String title = "";
    private int items_Counter = 0;
    private String image = "";
    private ArrayList<String> itemsUid;
    private String creatorUid = "";
    private LinkedList<String> sharedWithUidsList;

    public List() {
    }

    public String getTitle() {
        return title;
    }

    public List setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getItems_Counter() {
        return items_Counter;
    }

    public List setItems_Counter(int items_Counter) {
        this.items_Counter = items_Counter;
        return this;
    }

    public String getImage() {
        return image;
    }

    public List setImage(String image) {
        this.image = image;
        return this;
    }
}
