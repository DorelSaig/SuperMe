package com.DorelSaig.superme;

import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.superme.R;
import com.google.android.material.textview.MaterialTextView;

public class ListViewHolder extends RecyclerView.ViewHolder {

    AppCompatImageView list_IMG_image;
    MaterialTextView list_LBL_title;
    MaterialTextView list_LBL_counter;

    public ListViewHolder(View itemView) {
        super(itemView);
        this.list_IMG_image = itemView.findViewById(R.id.list_IMG_image);
        this.list_LBL_title = itemView.findViewById(R.id.list_LBL_title);
        this.list_LBL_counter = itemView.findViewById(R.id.list_LBL_counter);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


}
