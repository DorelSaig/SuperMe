package com.DorelSaig.superme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.Objects.List;
import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class Adapter_ShoppingLists extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<List> lists = new ArrayList<>();
    private ListItemClickListener listItemClickListener;

    public Adapter_ShoppingLists(Activity activity, ArrayList<List> lists) {
        this.activity = activity;
        this.lists = lists;
    }

    public Adapter_ShoppingLists setShoppingListClickListener(ListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lists_cards, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListViewHolder listViewHolder = (ListViewHolder) holder;
        List list = getItem(position);

        listViewHolder.list_LBL_title.setText(list.getTitle());
        listViewHolder.list_LBL_counter.setText(list.getItems_Counter() + " פריטים");

        Glide
                .with(activity)
                .load(list.getImage())
                .into(listViewHolder.list_IMG_image);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    private List getItem(int position){
        return lists.get(position);
    }




    private class ListViewHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView list_IMG_image;
        public MaterialTextView list_LBL_title;
        public MaterialTextView list_LBL_counter;

        public ListViewHolder(View itemView) {
            super(itemView);
            this.list_IMG_image = itemView.findViewById(R.id.list_IMG_image);
            this.list_LBL_title = itemView.findViewById(R.id.list_LBL_title);
            this.list_LBL_counter = itemView.findViewById(R.id.list_LBL_counter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listItemClickListener.listItemClicked(getItem(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }
}

