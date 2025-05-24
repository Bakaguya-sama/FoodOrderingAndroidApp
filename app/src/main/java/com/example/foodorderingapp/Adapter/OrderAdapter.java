package com.example.foodorderingapp.Adapter;

import static android.content.Intent.getIntent;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.Activity.CartActivity;
import com.example.foodorderingapp.Activity.OrderActivity;
import com.example.foodorderingapp.Activity.OrderDetailActivity;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.Domain.Order;
import com.example.foodorderingapp.Domain.orderlist;
import com.example.foodorderingapp.Helper.ManagmentCart;
import com.example.foodorderingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private ArrayList<Order> orderList;
    private Context context;
    private ManagmentCart managmentCart;
    private ArrayList<Foods> listItem=new ArrayList<>();
    private ItemOrderAdapter itemOrderAdapter;
    private LinearLayoutManager linearLayoutManager;



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


//    double totalAmount = 0;
//    for (Foods f : currentFoodsList) {
//        totalAmount += f.getPrice() * f.getNumberInCart();
//    }
    holder.total.setText("Total: $" + order.getTotal());
    holder.status.setText(order.getStatus());




    // 1. Tạo danh sách món ăn từ order.getOrderlists()
//    ArrayList<Foods> currentFoodsList = new ArrayList<>();
//    for (orderlist item : order.getOrderlists()) {
//        Foods foods = new Foods();
//        foods.setTitle(item.getItemname());
//        foods.setId(item.getId());
//        foods.setNumberInCart(item.getNum());
//        foods.setPrice(item.getPrice()); // đảm bảo có giá (nếu có trong orderlist)
//        foods.setImagePath(item.getImagePath()); // nếu có ảnh
//        currentFoodsList.add(foods);
//    }
    ArrayList<orderlist> currentOrderList = order.getOrderlists(); // dùng luôn danh sách

    if (currentOrderList.size() == 1) {
        holder.viewAll.setVisibility(View.GONE);
    }

    // 2. Gán dữ liệu vào RecyclerView bằng Adapter
    ItemOrderAdapter itemOrderAdapter = new ItemOrderAdapter(context, currentOrderList);
    itemOrderAdapter.setViewAll(false); // hiện toàn bộ món ăn
    holder.itemsContainer.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    holder.itemsContainer.setAdapter(itemOrderAdapter);

    holder.viewAll.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            itemOrderAdapter.setViewAll(true);
            holder.viewAll.setVisibility(View.GONE); // Ẩn "View All" sau khi nhấn
        }
    });

    // 3. Xử lý nút received và reorder
    if (order.getStatus().equals("DELIVERING")) {
        holder.received.setVisibility(View.VISIBLE);
        holder.reorder.setVisibility(View.GONE);
    } else {
        holder.received.setVisibility(View.GONE);
        holder.reorder.setVisibility(View.VISIBLE);
        holder.status.setTextColor(Color.parseColor("#4CAF50"));
    }

    // 4. Button "received" cập nhật trạng thái
    holder.received.setOnClickListener(v -> {
        order.setStatus("ORDER RECEIVED");
        holder.status.setText(order.getStatus());
        holder.status.setTextColor(Color.parseColor("#4CAF50"));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .collection("orders")
                    .document(order.getOrderid())
                    .update("status", "ORDER RECEIVED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Order marked as received", Toast.LENGTH_SHORT).show();
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            orderList.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, orderList.size());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update order", Toast.LENGTH_SHORT).show();
                        Log.e("OrderAdapter", "Firebase update failed", e);
                    });
        }

        context.startActivity(new Intent(context, OrderActivity.class).putExtra("ok", "ok"));
    });

    holder.reorder.setOnClickListener(v -> {
        ArrayList<Foods> foodToReorder = new ArrayList<>();
        for (orderlist ol : order.getOrderlists()) {
            Foods f = new Foods();
            f.setTitle(ol.getItemname());
            f.setPrice(ol.getPrice());
            f.setId(ol.getId());
            f.setImagePath(ol.getImagePath());
            f.setNumberInCart(ol.getNum());
            foodToReorder.add(f);
        }
        addToCart(foodToReorder);
    });

    holder.itemView.setOnClickListener(v -> {
        Order selectedOrder = orderList.get(position);

        // Truyền dữ liệu sang OrderDetailActivity
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("orderId", selectedOrder.getOrderid());
        context.startActivity(intent);
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
        TextView date, time, total, status, viewAll;
        RecyclerView itemsContainer;
        Button reorder, received;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            date = itemView.findViewById(R.id.order_date);
//            time = itemView.findViewById(R.id.order_time);
            total = itemView.findViewById(R.id.Total_value);
            status = itemView.findViewById(R.id.status);
            itemsContainer = itemView.findViewById(R.id.recyclerView_ItemOrder); // RecyclerView trong layout gốc
            reorder = itemView.findViewById(R.id.button2);
            received = itemView.findViewById(R.id.order_received_Btn);
            viewAll = itemView.findViewById(R.id.view_all_text);
        }
    }
}