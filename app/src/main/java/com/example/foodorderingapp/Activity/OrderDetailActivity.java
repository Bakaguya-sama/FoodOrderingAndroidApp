package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodorderingapp.Adapter.ItemOrderAdapter2;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.Domain.orderlist;
import com.example.foodorderingapp.Helper.ManagmentCart;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityOrderDetailBinding;
import com.example.foodorderingapp.databinding.ViewholderAddressBinding;
import com.example.foodorderingapp.databinding.YourOrderItem2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class OrderDetailActivity extends BaseActivity {

    private ActivityOrderDetailBinding binding;
    private ManagmentCart managementCart;
//    private boolean isOrderListViewAll = false;
    private ItemOrderAdapter2 adapter;
    private ArrayList<orderlist> orderItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        managementCart = new ManagmentCart(this);

        binding.imgViewBackOrderDetailActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnBuyAgainOrderDetailActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Foods> foodToReorder = new ArrayList<>();
                for (orderlist ol : orderItems) {
                    Foods f = new Foods();
                    f.setTitle(ol.getItemname());
                    f.setPrice(ol.getPrice());
                    f.setId(ol.getId());
                    f.setImagePath(ol.getImagePath());
                    f.setNumberInCart(ol.getNum());
                    foodToReorder.add(f);
                }
                addToCart(foodToReorder);
            }
        });

        View include_address = findViewById(R.id.include_address);
        ViewholderAddressBinding addressBinding = ViewholderAddressBinding.bind(include_address);

        View include_item_order2 = findViewById(R.id.include_item_order2);
        YourOrderItem2Binding itemBinding = YourOrderItem2Binding.bind(include_item_order2);

        adapter = new ItemOrderAdapter2(this, orderItems);
        adapter.setViewAll(true);
        itemBinding.recyclerViewItemOrder.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        itemBinding.recyclerViewItemOrder.setAdapter(adapter);
        itemBinding.recyclerViewItemOrder.post(() -> {
            ViewGroup.LayoutParams params = itemBinding.recyclerViewItemOrder.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            itemBinding.recyclerViewItemOrder.setLayoutParams(params);
            itemBinding.recyclerViewItemOrder.requestLayout(); // 👈 THÊM DÒNG NÀY
        });
        AtomicReference<Double> total = new AtomicReference<>((double) 0);
        AtomicReference<Double> tax = new AtomicReference<>(0.02);

        itemBinding.recyclerViewItemOrder.setNestedScrollingEnabled(false);
        itemBinding.recyclerViewItemOrder.setHasFixedSize(false);

        itemBinding.viewAllText.setVisibility(View.GONE);

//        if (orderItems.size() < 2) {
//            itemBinding.viewAllText.setVisibility(View.GONE);
//        }

        //Get data
        String orderId = getIntent().getStringExtra("orderId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && orderId != null) {
            String userId = user.getUid();

            db.collection("Users")
                    .document(userId)
                    .collection("orders")
                    .document(orderId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Địa chỉ giao hàng
                            String address = documentSnapshot.getString("address.address");
                            String addressName = documentSnapshot.getString("address.addressName");

                            // Binding dữ liệu vào layout include viewholder_address
                            addressBinding.txtViewAddressNameViewholderAddress.setText(addressName);
                            addressBinding.txtViewAddressViewholderAddress.setText(address);
                            addressBinding.txtViewAddressBoxViewholderAddress.setText(addressName);
                            addressBinding.txtViewEditViewholderAddress.setVisibility(View.GONE);

                            // Nếu có danh sách món ăn
                            List<Map<String, Object>> orderLists = (List<Map<String, Object>>) documentSnapshot.get("orderlists");

                            if (orderLists != null) {
                                for (Map<String, Object> item : orderLists) {
                                    int id = ((Long) item.get("id")).intValue();
                                    String itemname = (String) item.get("itemname");
                                    double price = (Double) item.get("price");
                                    int num = ((Long) item.get("num")).intValue();
                                    String imagePath = (String) item.get("imagePath");
                                    total.updateAndGet(v -> new Double((double) (v + price * num)));

                                    orderlist orderItem = new orderlist(itemname, num, id, price, imagePath);
                                    orderItems.add(orderItem);
                                }

                                // Dữ liệu đã sẵn sàng trong orderItems
                                for (orderlist oi : orderItems) {
                                    Log.d("OrderItem", oi.getItemname() + " x" + oi.getNum());
                                }

//                                LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
//                                itemBinding.recyclerViewItemOrder.setLayoutManager(linearLayoutManager);
//                                ItemOrderAdapter adapter=new ItemOrderAdapter(this, orderItems);
//                                adapter.setViewAll(isOrderListViewAll);
//                                itemBinding.recyclerViewItemOrder.setAdapter(adapter);

                                itemBinding.txtViewMerchandiseSubtotalOrderDetailActivity.setText("$" + total.get());

                                Double totalFromDb = documentSnapshot.getDouble("total");
                                if (totalFromDb != null) {
                                    itemBinding.TotalValue.setText(String.format("$%.2f", totalFromDb));
                                }

                                tax.set(total.get() * 0.02);
                                tax.set(Math.round(tax.get() * 100.0) / 100.0);
                                itemBinding.txtViewTaxOrderDetailActivity.setText(String.format("$%.2f", tax.get()));

                                adapter.setItemList(orderItems); // 👈 bạn cần viết thêm hàm này trong ItemOrderAdapter
//                                adapter.setViewAll(true);
                                adapter.notifyDataSetChanged();
                                // ViewAll click

                                // Di chuyển đoạn này vào đây:
//                                if (orderItems.size() < 2) {
//                                    itemBinding.viewAllText.setVisibility(View.GONE);
//                                } else {
//                                    itemBinding.viewAllText.setVisibility(View.VISIBLE); // THÊM DÒNG NÀY
//                                }

//                                itemBinding.viewAllText.setOnClickListener(v -> {
//                                    isOrderListViewAll = true; // CẬP NHẬT BIẾN TOÀN CỤC
//                                    adapter.setViewAll(true);  // Cập nhật trạng thái adapter
//
//                                    itemBinding.recyclerViewItemOrder.post(() -> {
//                                        ViewGroup.LayoutParams params = itemBinding.recyclerViewItemOrder.getLayoutParams();
//                                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                                        itemBinding.recyclerViewItemOrder.setLayoutParams(params);
//                                        itemBinding.recyclerViewItemOrder.requestLayout();
//                                    });
//
//                                    itemBinding.viewAllText.setVisibility(View.GONE);
//                                });
                            }
                            // OrderID, thời gian
                            binding.txtViewOrderIDOrderDetailActivity.setText(orderId);
                            String status = documentSnapshot.getString("status");
                            if (status != null) {
                                if (status.equals("DELIVERING")) {
                                    binding.txtViewStatusOrderDetailActivity.setText("Your Order is Not Completed Yet");
                                    binding.txtViewStatus2OrderDetailActivity.setText("Preparing");
                                    binding.txtViewTimeOfOrderOrderDetailActivity.setText(documentSnapshot.getString("date") + " " + documentSnapshot.getString("time"));
                                    binding.bottomButtonContainer.setVisibility(View.GONE);
                                    binding.txtViewStatusOrderDetailActivity.setBackgroundColor(Color.RED);
                                    }
                                else {
                                    binding.txtViewStatusOrderDetailActivity.setText("Your Order is Completed");
                                    binding.txtViewStatus2OrderDetailActivity.setText("Receiving successfully");
                                    binding.txtViewTimeOfOrderOrderDetailActivity.setText(documentSnapshot.getString("date") + " " + documentSnapshot.getString("time"));
                                }
                            }
                        } else {
                            Log.d("OrderDetail", "Order không tồn tại");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("OrderDetail", "Lỗi khi lấy đơn hàng", e));
        }
    }

    private void addToCart(ArrayList<Foods> listItem) {
        DatabaseReference foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
        ArrayList<Foods> fullFoodsList = new ArrayList<>();
        final int[] loadedCount = {0};

        for (Foods item : listItem) {
            String title = item.getTitle();

            foodsRef.orderByChild("Title").equalTo(title)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean found = false;
                            for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                                Foods fullFood = foodSnapshot.getValue(Foods.class);
                                if (fullFood != null) {
                                    fullFood.setNumberInCart(item.getNumberInCart());
                                    fullFoodsList.add(fullFood);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                // Nếu không tìm thấy, vẫn add bản tạm
                                fullFoodsList.add(item);
                            }

                            loadedCount[0]++;
                            if (loadedCount[0] == listItem.size()) {
                                managementCart.addlist(fullFoodsList);
                                Intent intent = new Intent(OrderDetailActivity.this, CartActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(OrderDetailActivity.this, "Lỗi tải món: " + title, Toast.LENGTH_SHORT).show();
                            loadedCount[0]++;
                            if (loadedCount[0] == listItem.size()) {
                                managementCart.addlist(fullFoodsList);
                                Intent intent = new Intent(OrderDetailActivity.this, CartActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }
    }


}