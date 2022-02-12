package com.DorelSaig.superme.Objects;

import java.util.UUID;

public class MyItem {

    private String itemUid;
    private String itemTitle = "";
    private float amount = 0;
    private String amountSuffix = "יח'";
    private String itemImage = "https://firebasestorage.googleapis.com/v0/b/superme-e69d5.appspot.com/o/images%2Fimg_default_list_cover.jpg?alt=media&token=60b8db0a-91fd-4a10-9bc3-47418f158da1";
    private String itemIcon = "";
    private String notes = "";
    private String creatorUid = "System";

    public MyItem() {    }

    public MyItem(String itemTitle, float amount, String creatorUid) {
        this.itemUid = UUID.randomUUID().toString();
        this.itemTitle = itemTitle;
        this.amount = amount;
        this.itemImage = "https://firebasestorage.googleapis.com/v0/b/superme-e69d5.appspot.com/o/images%2Fimg_default_list_cover.jpg?alt=media&token=60b8db0a-91fd-4a10-9bc3-47418f158da1";
        this.creatorUid = creatorUid;
    }

    public String getItemUid() {
        return itemUid;
    }

    public MyItem setItemUid(String itemUid) {
        this.itemUid = itemUid;
        return this;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public MyItem setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
        return this;
    }

    public float getAmount() {
        return amount;
    }

    public MyItem setAmount(float amount) {
        this.amount = amount;
        return this;
    }

    public String getAmountSuffix() {
        return amountSuffix;
    }

    public MyItem setAmountSuffix(String amountSuffix) {
        this.amountSuffix = amountSuffix;
        return this;
    }

    public String getItemImage() {
        return itemImage;
    }

    public MyItem setItemImage(String itemImage) {
        this.itemImage = itemImage;
        return this;
    }

    public String getItemIcon() {
        return itemIcon;
    }

    public MyItem setItemIcon(String itemIcon) {
        this.itemIcon = itemIcon;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public MyItem setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public MyItem setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
        return this;
    }
}
