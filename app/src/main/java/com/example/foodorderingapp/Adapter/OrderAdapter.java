package com.example.foodorderingapp.Adapter;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.Activity.CartActivity;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.Domain.Order;
import com.example.foodorderingapp.Domain.orderlist;
import com.example.foodorderingapp.Helper.ManagmentCart;
import com.example.foodorderingapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private ArrayList<Order> orderList;
    private Context context;
    private ManagmentCart managmentCart;

private ArrayList<Foods> listItem=new ArrayList<>();
    public OrderAdapter(Context context, ArrayList<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_order_item, parent, false);
        return new ViewHolder(inflate);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        managmentCart = new ManagmentCart(context);
        Order order = orderList.get(position);

        holder.date.setText(order.getDate());
        holder.time.setText(order.getTime());
        holder.total.setText("$" + order.getTotal());

        holder.itemsContainer.removeAllViews();

        ArrayList<Foods> currentFoodsList = new ArrayList<>(); // Danh sách món của đơn hàng hiện tại

        for (orderlist item : order.getOrderlists()) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.layout, holder.itemsContainer, false);

            TextView txtItem = itemView.findViewById(R.id.name);
            TextView txtNum = itemView.findViewById(R.id.num);

            txtItem.setText(item.getItemname());
            txtNum.setText("x" + item.getNum());

            holder.itemsContainer.addView(itemView);

            Foods foods = new Foods();
            foods.setTitle(item.getItemname());
            foods.setId(item.getId());
            foods.setNumberInCart(item.getNum());
            currentFoodsList.add(foods);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart(currentFoodsList);
            }
        });
    }





    private void addToCart(ArrayList<Foods> listItem) {
        DatabaseReference foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
        ArrayList<Foods> fullFoodsList = new ArrayList<>();

        for (Foods item : listItem) {
            String title = item.getTitle();

            foodsRef.orderByChild("Title").equalTo(title)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                                Foods fullFood = foodSnapshot.getValue(Foods.class);
                                if (fullFood != null) {
                                    fullFood.setNumberInCart(item.getNumberInCart());
                                    fullFoodsList.add(fullFood);
                                }

                                if (fullFoodsList.size() == listItem.size()) {
                                    managmentCart.addlist(fullFoodsList);
                                    Intent intent = new Intent(context, CartActivity.class);
                                    context.startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi tải món: " + title, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, time, total;
        LinearLayout itemsContainer; // Thay ListView bằng LinearLayout
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.order_date);
            time = itemView.findViewById(R.id.order_time);
            total = itemView.findViewById(R.id.Total_value);
            itemsContainer = itemView.findViewById(R.id.orderitemscontainer); // ID giữ nguyên, chỉ thay đổi kiểu
            button = itemView.findViewById(R.id.button2);
        }
    }
}