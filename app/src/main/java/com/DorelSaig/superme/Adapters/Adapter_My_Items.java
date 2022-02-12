package com.DorelSaig.superme.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.Listeners.ItemsClickListener;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.R;
import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class Adapter_My_Items extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context activity;
    private ArrayList<MyItem> items;
    private ItemsClickListener itemsClickListener;

    public Adapter_My_Items(Context activity, ArrayList<MyItem> items) {
        this.activity = activity;
        this.items = items;
    }

    public Adapter_My_Items setItemClickListener(ItemsClickListener itemsClickListener) {
        this.itemsClickListener = itemsClickListener;
        return this;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_item_cards, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        MyItem item = getItem(position);
        itemViewHolder.myItem_LBL_title.setText(item.getItemTitle());

        Glide
                .with(activity)
                .load(Uri.parse(item.getItemImage()))
                .into(itemViewHolder.myItem_IMG_image);



        //itemViewHolder.myItem_IMG_image.setImageURI(Uri.parse(item.getItemImage()));

        Log.d("pttt", item.getItemImage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public MyItem getItem(int position){
        return items.get(position);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView myItem_IMG_image;
        public MaterialTextView myItem_LBL_title;


        public ItemViewHolder(View itemView) {
            super(itemView);
            this.myItem_IMG_image = itemView.findViewById(R.id.myItem_IMG_image);
            this.myItem_LBL_title = itemView.findViewById(R.id.myItem_LBL_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemsClickListener.itemClicked(getItem(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }



}
