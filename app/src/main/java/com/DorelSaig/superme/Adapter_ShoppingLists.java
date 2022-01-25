package com.DorelSaig.superme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.Objects.MyList;
import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class Adapter_ShoppingLists extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context activity;
    private ArrayList<MyList> lists = new ArrayList<>();
    private ListItemClickListener listItemClickListener;

    public Adapter_ShoppingLists(Context activity, ArrayList<MyList> lists) {
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
        MyList myList = getItem(position);

        listViewHolder.list_LBL_title.setText(myList.getTitle());
        listViewHolder.list_LBL_counter.setText(myList.getItems_Counter() + " פריטים");

        Glide
                .with(activity)
                .load(myList.getImage_cover())
                .into(listViewHolder.list_IMG_image);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    private MyList getItem(int position){
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

