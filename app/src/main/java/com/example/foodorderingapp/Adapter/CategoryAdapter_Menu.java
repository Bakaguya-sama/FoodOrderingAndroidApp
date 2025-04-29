package com.example.foodorderingapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderingapp.Domain.Category;
import com.example.foodorderingapp.R;

import java.util.ArrayList;

public class CategoryAdapter_Menu extends RecyclerView.Adapter<CategoryAdapter_Menu.CategoryViewHolder> {
    private Context context;
    private ArrayList<Category> categories;
    private int selectedPosition = 0;

    public CategoryAdapter_Menu(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_category_menu, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.catNameText_Menu.setText(category.getName());

        // Chuyển tên ảnh thành resourceId
        int imageResId = context.getResources().getIdentifier(
                category.getImagePath(), "drawable", context.getPackageName());

        // Gán ảnh vào ImageView
        holder.imgCat_Menu.setImageResource(imageResId);

        // Đổi màu theo trạng thái chọn
        if (position == selectedPosition) {
            holder.imgCat_Menu.setColorFilter(ContextCompat.getColor(context, R.color.red));
            holder.catNameText_Menu.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.imgCat_Menu.setColorFilter(ContextCompat.getColor(context, R.color.grey_menu));
            holder.catNameText_Menu.setTextColor(ContextCompat.getColor(context, R.color.grey_menu));
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();

            // Notify listener
            if (listener != null) {
                listener.onCategoryClick(category.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCat_Menu;
        TextView catNameText_Menu;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCat_Menu = itemView.findViewById(R.id.imgCat_Menu);
            catNameText_Menu = itemView.findViewById(R.id.catNameText_Menu);
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(int categoryId);
    }

    private OnCategoryClickListener listener;

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
}
