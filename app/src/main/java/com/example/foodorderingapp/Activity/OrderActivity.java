package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodorderingapp.Adapter.OrderAdapter;
import com.example.foodorderingapp.Domain.Order;
import com.example.foodorderingapp.Helper.ManagmentCart;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class OrderActivity extends BaseActivity {

    private OrderAdapter adapter;
    private ArrayList<Order> orderList;
    private ManagmentCart managmentCart;
    private ActivityOrderBinding binding;

    // Mặc định lọc theo trạng thái này
    private String currentStatusFilter = "DELIVERING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentViewInBase(binding.getRoot());
        setupBottomNav();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        initList();
        Intent intent = getIntent();
        String ok = intent.getStringExtra("ok");

        if (ok != null) {
            Log.d("OrderActivity", "Received: " + ok);
            currentStatusFilter = "ORDER RECEIVED";
            binding.deliveringimg.setImageResource(R.drawable.food_delivering);
            binding.receiveimg.setImageResource(R.drawable.clicked_food_delivered);
            binding.deliveringTxt.setTextColor(Color.parseColor("#6E6E6E"));
            binding.receiveTxt.setTextColor(Color.parseColor("#D35400"));
            filterOrdersByStatus(currentStatusFilter);

            ok = null;
        }

        // Click - Đang giao hàng
        binding.deliveringLl.setOnClickListener(v -> {
            currentStatusFilter = "DELIVERING";
            binding.deliveringimg.setImageResource(R.drawable.cliked_food_delivering);
            binding.receiveimg.setImageResource(R.drawable.food_delivered_icon);
            binding.deliveringTxt.setTextColor(Color.parseColor("#C0392B"));
            binding.receiveTxt.setTextColor(Color.parseColor("#6E6E6E"));

            filterOrdersByStatus(currentStatusFilter);
        });

        // Click - Đã nhận hàng
        binding.received.setOnClickListener(v -> {
            currentStatusFilter = "ORDER RECEIVED";
            binding.deliveringimg.setImageResource(R.drawable.food_delivering);
            binding.receiveimg.setImageResource(R.drawable.clicked_food_delivered);
            binding.deliveringTxt.setTextColor(Color.parseColor("#6E6E6E"));
            binding.receiveTxt.setTextColor(Color.parseColor("#D35400"));
            filterOrdersByStatus(currentStatusFilter);
        });
    }

    private void filterOrdersByStatus(String status) {
        ArrayList<Order> filteredList = new ArrayList<>();
        for (Order order : orderList) {
            if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(order);
            }
        }

        adapter = new OrderAdapter(this, filteredList);
        binding.orderlist.setAdapter(adapter);

        if (!filteredList.isEmpty()) {
            binding.orderlist.setVisibility(View.VISIBLE);
            binding.imageView5.setVisibility(View.GONE);
            binding.textView24.setVisibility(View.GONE);
        } else {
            binding.orderlist.setVisibility(View.GONE);
            binding.textView24.setVisibility(View.VISIBLE);
            binding.imageView5.setVisibility(View.VISIBLE);
        }
    }

    private void initList() {
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, new ArrayList<>());
        binding.orderlist.setLayoutManager(new LinearLayoutManager(this));
        binding.orderlist.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        binding.orderlist.addItemDecoration(dividerItemDecoration);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String currentUserId = user.getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        orderList.add(order);
                    }

                    // Sắp xếp theo thời gian giảm dần
                    Collections.sort(orderList, (o1, o2) -> {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd,yyyy hh:mm a", Locale.US);
                            Date d1 = sdf.parse(o1.getDate() + " " + o1.getTime());
                            Date d2 = sdf.parse(o2.getDate() + " " + o2.getTime());
                            return d2.compareTo(d1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

                    filterOrdersByStatus(currentStatusFilter); // Lọc đơn hàng theo trạng thái mặc định
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to load orders", e);
                });
    }

    private void deleteAllUserOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String currentUserId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ordersRef = db.collection("users")
                .document(currentUserId)
                .collection("orders");

        ordersRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                orderList.clear();
                                adapter.notifyDataSetChanged();
                                binding.orderlist.setVisibility(View.GONE);
                                binding.textView24.setVisibility(View.VISIBLE);
                                binding.imageView5.setVisibility(View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase", "Error deleting orders", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error getting orders", e);
                });
    }
}
