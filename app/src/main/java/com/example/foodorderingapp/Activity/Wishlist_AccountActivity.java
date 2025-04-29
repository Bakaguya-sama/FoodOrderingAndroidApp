package com.example.foodorderingapp.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.Adapter.Item_Menu;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityWishlistAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Wishlist_AccountActivity extends BaseActivity {
    private ActivityWishlistAccountBinding binding;
    private ArrayList<Foods> foodsList = new ArrayList<>();
    private Set<String> wishlistIds = new HashSet<>();
    private String userId;
    private Item_Menu adapter; // Để lưu adapter ra ngoài cho dễ notifyDataSetChanged nếu cần

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityWishlistAccountBinding.inflate(getLayoutInflater());
        setContentViewInBase(binding.getRoot());
        setupBottomNav();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.imgViewBackWishlistAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        db.collection("Users")
//                .document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Map<String, Object> wishlistMap = (Map<String, Object>) documentSnapshot.get("wishlist");
//
//                        Set<String> wishlistIds = new HashSet<>();
//                        if (wishlistMap != null) {
//                            wishlistIds.addAll(wishlistMap.keySet());
//                        }
//
//                        // 👉 Chỉ lấy foods có trong wishlist
//                        List<Foods> wishlistFoods = new ArrayList<>();
//                        for (Foods food : foodsList) {
//                            if (wishlistIds.contains(String.valueOf(food.getId()))) {
//                                wishlistFoods.add(food);
//                            }
//                        }
//
//                        // Sau khi lấy xong wishlistIds, tạo adapter
//                        Item_Menu adapter = new Item_Menu(wishlistFoods, this, userId, wishlistIds);
//
//                        binding.listViewWishlistAccountActivity.setAdapter(adapter); // gán adapter cho ListView
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Nếu thất bại, vẫn khởi tạo adapter với set rỗng
//                    Item_Menu adapter = new Item_Menu(foodsList, this, userId, new HashSet<>());
//                    binding.listViewWishlistAccountActivity.setAdapter(adapter);
//                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFoodList(); // Load lại danh sách món ăn
    }

    private void initFoodList() {
        DatabaseReference foodRef = database.getReference("Foods");

        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodsList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Foods food = child.getValue(Foods.class);
                        if (food != null) {
                            foodsList.add(food);
                        }
                    }
                }
                loadWishlist();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(MenuActivity.this, "Lỗi khi tải danh sách món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWishlist() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    wishlistIds.clear();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> wishlistMap = (Map<String, Object>) documentSnapshot.get("wishlist");
                        if (wishlistMap != null) {
                            wishlistIds.addAll(wishlistMap.keySet());
                        }
                    }

                    if (wishlistIds.isEmpty()) {
                        binding.txtViewNoWishlistWishlistAccountActivity.setVisibility(View.VISIBLE);
                        binding.listViewWishlistAccountActivity.setVisibility(View.GONE);
                    } else {
                        binding.txtViewNoWishlistWishlistAccountActivity.setVisibility(View.GONE);
                        binding.listViewWishlistAccountActivity.setVisibility(View.VISIBLE);

                        // Lọc danh sách món ăn theo wishlist
                        List<Foods> wishlistFoods = new ArrayList<>();
                        for (Foods food : foodsList) {
                            if (wishlistIds.contains(String.valueOf(food.getId()))) {
                                wishlistFoods.add(food);
                            }
                        }

                        // Tạo adapter và gán lại ListView
                        if (adapter == null) {
                            adapter = new Item_Menu(wishlistFoods, this, userId, wishlistIds, this::loadWishlist);
                            binding.listViewWishlistAccountActivity.setAdapter(adapter);
                        } else {
                            adapter.updateData(wishlistFoods, wishlistIds);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Nếu thất bại, tạo adapter với danh sách rỗng
                    if (adapter == null) {
                        adapter = new Item_Menu(new ArrayList<>(), this, userId, wishlistIds, this::loadWishlist);
                        binding.listViewWishlistAccountActivity.setAdapter(adapter);
                    } else {
                        adapter.updateData(new ArrayList<>(), wishlistIds);
                    }
                });
    }
}