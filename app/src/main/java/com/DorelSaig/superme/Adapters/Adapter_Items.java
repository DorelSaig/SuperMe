package com.DorelSaig.superme.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.ItemsClickListener;
import com.DorelSaig.superme.Objects.MyItem;
import com.DorelSaig.superme.R;
import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class Adapter_Items extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context activity;
    private ArrayList<MyItem> items = new ArrayList<>();
    private ItemsClickListener itemsClickListener;

    public Adapter_Items(Context activity, ArrayList<MyItem> items) {
        this.activity = activity;
        this.items = items;
    }

    public Adapter_Items setItemClickListener(ItemsClickListener itemsClickListener) {
        this.itemsClickListener = itemsClickListener;
        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cards, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListViewHolder listViewHolder = (ListViewHolder) holder;
        MyItem item = getItem(position);

        listViewHolder.item_LBL_title.setText(item.getItemTitle());
        listViewHolder.item_LBL_amount.setText(String.format("%s %s", item.getAmount(), item.getAmountSuffix()));

        Glide
                .with(activity)
                .load(item.getItemIcon())
                .into(listViewHolder.item_IMG_icon);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public MyItem getItem(int position){
        return items.get(position);
    }




    private class ListViewHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView item_IMG_icon;
        public MaterialTextView item_LBL_title;
        public MaterialTextView item_LBL_amount;

        public ListViewHolder(View itemView) {
            super(itemView);
            this.item_IMG_icon = itemView.findViewById(R.id.item_IMG_icon);
            this.item_LBL_title = itemView.findViewById(R.id.item_LBL_title);
            this.item_LBL_amount = itemView.findViewById(R.id.item_LBL_amount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemsClickListener.itemClicked(getItem(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }
}

