package com.DorelSaig.superme.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.DorelSaig.superme.Listeners.CategoryClickListener;
import com.DorelSaig.superme.Objects.MyCategory;
import com.DorelSaig.superme.R;
import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class Adapter_My_Categories extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context activity;
    private ArrayList<MyCategory> categories;
    private CategoryClickListener categoryClickListener;

    public Adapter_My_Categories(Context activity, ArrayList<MyCategory> categories) {
        this.activity = activity;
        this.categories = categories;
    }

    public Adapter_My_Categories setCategoryClickListener(CategoryClickListener categoryClickListener) {
        this.categoryClickListener = categoryClickListener;
        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
        MyCategory category = getItem(position);

        categoryViewHolder.category_LBL_title.setText(category.getTitle());

        Glide
                .with(activity)
                .load(Uri.parse(category.getCat_cover()))
                .into(categoryViewHolder.category_IMG_image);

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public MyCategory getItem(int position) {
        return categories.get(position);
    }

    private class CategoryViewHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView category_IMG_image;
        public MaterialTextView category_LBL_title;


        public CategoryViewHolder(View categoryView) {
            super(categoryView);
            this.category_IMG_image = itemView.findViewById(R.id.category_IMG_image);
            this.category_LBL_title = itemView.findViewById(R.id.category_LBL_title);

            categoryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryClickListener.categoryClicked(getItem(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }
}
