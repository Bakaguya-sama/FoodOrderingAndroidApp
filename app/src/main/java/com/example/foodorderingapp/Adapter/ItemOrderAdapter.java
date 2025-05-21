package com.example.foodorderingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.Domain.orderlist;
import com.example.foodorderingapp.R;

import java.util.ArrayList;

public class ItemOrderAdapter extends RecyclerView.Adapter<ItemOrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<orderlist> itemList;

    public ItemOrderAdapter(Context context, ArrayList<orderlist> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemOrderAdapter.ViewHolder holder, int position) {
        orderlist item = itemList.get(position);
        holder.title.setText(item.getItemname());
        holder.quantity.setText("x" + item.getNum());
        holder.price.setText("$" + String.format("%.2f", item.getPrice()));

        // Load image
        Glide.with(context)
                .load(item.getImagePath()) // hoặc item.getImage(), tuỳ tên field ảnh bạn dùng
                .placeholder(R.drawable.food_theme) // ảnh tạm
                .into(holder.pic);
    }

    @Override
    public int getItemCount() {
        if (itemList == null) return 0;
        return isViewAll ? itemList.size() : Math.min(1, itemList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, quantity, price;
        ImageView pic;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.txtView_Title_ItemOrder);
            quantity = itemView.findViewById(R.id.txtView_Quantity_ItemOrder);
            price = itemView.findViewById(R.id.txtView_Price_ItemOrder);
            pic = itemView.findViewById(R.id.imgView_ItemOrder);
        }
    }

    private boolean isViewAll = false;

    public void setViewAll(boolean viewAll) {
        isViewAll = viewAll;
        notifyDataSetChanged();
    }

}
