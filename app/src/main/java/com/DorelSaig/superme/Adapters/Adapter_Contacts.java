package com.DorelSaig.superme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.Objects.MyContact;
import com.DorelSaig.superme.R;
import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter_Contacts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context activity;
    private ArrayList<MyContact> items;

    public Adapter_Contacts(Activity activity, ArrayList<MyContact> items) {
        this.activity = activity;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_cards, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
        MyContact contact = getItem(position);
        contactViewHolder.contact_LBL_name.setText(contact.getName());

        Glide
                .with(activity)
                .load(Uri.parse(contact.getImgUrl()))
                .override(200,200)
                .into(contactViewHolder.contact_IMG_profile);

        Log.d("pttt", contact.getImgUrl());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public MyContact getItem(int position){
        return items.get(position);
    }



    private class ContactViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView contact_IMG_profile;
        private MaterialTextView contact_LBL_name;

        public ContactViewHolder(View itemView) {
            super(itemView);
            this.contact_IMG_profile = itemView.findViewById(R.id.contact_IMG_profile);
            this.contact_LBL_name = itemView.findViewById(R.id.contact_LBL_name);

        }
    }
}
