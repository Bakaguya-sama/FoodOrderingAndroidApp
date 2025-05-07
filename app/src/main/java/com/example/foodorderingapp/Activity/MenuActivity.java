package com.example.foodorderingapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.Adapter.BestFoodsAdapter;
import com.example.foodorderingapp.Adapter.CategoryAdapter;
import com.example.foodorderingapp.Adapter.CategoryAdapter_Menu;
import com.example.foodorderingapp.Adapter.Item_Menu;
import com.example.foodorderingapp.Adapter.SpacesItemDecoration;
import com.example.foodorderingapp.Domain.Category;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAccountBinding;
import com.example.foodorderingapp.databinding.ActivityMainBinding;
import com.example.foodorderingapp.databinding.ActivityMenuBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MenuActivity extends BaseActivity {
    private int currentCategoryId = -1; // -1 nghĩa là chưa chọn category
    RecyclerView recyclerView;
    private ArrayList<Foods> allFoods = new ArrayList<>();
    Item_Menu itemAdapter;
    private Set<String> wishlistIds = new HashSet<>();
    CategoryAdapter_Menu adapter;
    private ActivityMenuBinding binding;
    private ArrayList<Category> categoryList = new ArrayList<>();

    Spinner locationSpinner, timeSpinner, priceSpinner;
    String userId;
    FirebaseFirestore db;

    @Override
    protected void onResume() {
        super.onResume();
        initCategory();
        initFoodList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        binding= ActivityMenuBinding.inflate(getLayoutInflater());
//        super.setContentView(R.layout.activity_menu);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentViewInBase(binding.getRoot());
        setupBottomNav();

//        initCategory();
//        initFoodList();


        locationSpinner = findViewById(R.id.locationSpinner);
        timeSpinner = findViewById(R.id.timeSpinner);
        priceSpinner = findViewById(R.id.priceSpinner);

        //UserId
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        loadWishlist();

        // Adapter cho Location
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this, R.array.location_options, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

    // Adapter cho Time
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

    // Adapter cho Price
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(priceAdapter);

    //   locationSpinner.setBackgroundResource(R.drawable.spinner_selected);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                highlightSelected(view, position, true);
                // Location luôn được áp dụng cùng với time và price hiện tại
                filterFoods(
                        position,
                        timeSpinner.getSelectedItemPosition(),
                        priceSpinner.getSelectedItemPosition()
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                highlightSelected(view, position, false);
                if (position != 0) priceSpinner.setSelection(0); // chỉ reset nếu không phải "Tất cả"
                filterFoods(locationSpinner.getSelectedItemPosition(), position, priceSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        priceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                highlightSelected(view, position, false);
                if (position != 0) timeSpinner.setSelection(0); // chỉ reset nếu không phải "Tất cả"
                filterFoods(locationSpinner.getSelectedItemPosition(), timeSpinner.getSelectedItemPosition(), position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void highlightSelected(View view, int position, boolean alwaysBlue) {
        if (view instanceof TextView) {
            TextView selectedText = (TextView) view;
            if (alwaysBlue || position != 0) {
                selectedText.setTextColor(getResources().getColor(R.color.selected_spinner_blue)); // Xanh
            } else {
                selectedText.setTextColor(getResources().getColor(R.color.black)); // Mặc định
            }
        }
    }

    private void initCategory() {
        DatabaseReference categoryRef = database.getReference("Category");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Category category = child.getValue(Category.class);
                        if (category != null) {
                            categoryList.add(category);
                        }
                    }

                    if (!categoryList.isEmpty()) {
                        binding.recyclerViewMenuMenu.setLayoutManager(new LinearLayoutManager(MenuActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        adapter = new CategoryAdapter_Menu(MenuActivity.this, categoryList);
                        binding.recyclerViewMenuMenu.setAdapter(adapter);

                        int spacingInPixels = 72; // hoặc 32 tuỳ thích
                        binding.recyclerViewMenuMenu.addItemDecoration(new SpacesItemDecoration(spacingInPixels));


                        currentCategoryId = categoryList.get(0).getId(); // ✅ Gán mặc định

                        adapter.setOnCategoryClickListener(categoryId -> {
                            currentCategoryId = categoryId;
                            filterFoods(
                                    locationSpinner.getSelectedItemPosition(),
                                    timeSpinner.getSelectedItemPosition(),
                                    priceSpinner.getSelectedItemPosition()
                            );
                        });

                        // ✅ Nếu món ăn đã load => apply filter
                        if (!allFoods.isEmpty()) {
                            filterFoods(locationSpinner.getSelectedItemPosition(),
                                    timeSpinner.getSelectedItemPosition(),
                                    priceSpinner.getSelectedItemPosition());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(MenuActivity.this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initFoodList() {
        DatabaseReference foodRef = database.getReference("Foods");

        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFoods.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Foods food = child.getValue(Foods.class);
                        if (food != null) {
                            allFoods.add(food);
                        }
                    }


                    if (currentCategoryId != -1) {
                        filterFoods(
                                locationSpinner.getSelectedItemPosition(),
                                timeSpinner.getSelectedItemPosition(),
                                priceSpinner.getSelectedItemPosition()
                        );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MenuActivity.this, "Lỗi khi tải danh sách món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void showFoodsByCategory(int categoryId) {
//        ArrayList<Foods> filteredFoods = new ArrayList<>();
//        for (Foods food : allFoods) {
//            if (food.getCategoryId() == categoryId) {
//                filteredFoods.add(food);
//            }
//        }
//
//        // Set adapter cho danh sách món ăn (RecyclerView khác, ví dụ: recyclerViewFoodList)
//        itemAdapter = new Item_Menu(filteredFoods, this, userId, wishlistIds, () -> {
//            recreate();
//        });
//        binding.listViewItemMenu.setAdapter(itemAdapter);
//    }

    private void showFoodsByCategory(int categoryId) {
        currentCategoryId = categoryId;
        filterFoods(
                locationSpinner.getSelectedItemPosition(),
                timeSpinner.getSelectedItemPosition(),
                priceSpinner.getSelectedItemPosition()
        );
    }


    private void filterFoods(int locationId, int timeSort, int priceSort) {
        ArrayList<Foods> filteredFoods = new ArrayList<>();

        for (Foods food : allFoods) {
            if (food.getLocationId() == locationId && food.getCategoryId() == currentCategoryId) {
                filteredFoods.add(food);
            }
        }

        // 👉 Nếu chọn "Tất cả" (position = 0), thì không sắp xếp
        if (timeSort != 0) {
            if (timeSort == 1) {
                filteredFoods.sort(Comparator.comparingInt(Foods::getTimeValue));
            } else if (timeSort == 2) {
                filteredFoods.sort((f1, f2) -> Integer.compare(f2.getTimeValue(), f1.getTimeValue()));
            }
        }

        if (priceSort != 0) {
            if (priceSort == 1) {
                filteredFoods.sort(Comparator.comparingDouble(Foods::getPrice));
            } else if (priceSort == 2) {
                filteredFoods.sort((f1, f2) -> Double.compare(f2.getPrice(), f1.getPrice()));
            }
        }

        if (filteredFoods.isEmpty()) {
            //Toast.makeText(MenuActivity.this, "Không có món ăn phù hợp", Toast.LENGTH_SHORT).show();
        }

        // 🧠 Lưu lại vị trí cuộn hiện tại của ListView
        int scrollPosition = binding.listViewItemMenu.getFirstVisiblePosition();

        // Nếu đã có adapter thì chỉ cập nhật dữ liệu
        if (itemAdapter != null) {
            itemAdapter.updateData(filteredFoods, wishlistIds);
            binding.listViewItemMenu.setSelection(scrollPosition); // trở lại vị trí cũ
        } else {
            // Nếu chưa có adapter, khởi tạo mới
            itemAdapter = new Item_Menu(filteredFoods, this, userId, wishlistIds, () -> {
                // Gọi lại filter khi like/dislike
                filterFoods(
                        locationSpinner.getSelectedItemPosition(),
                        timeSpinner.getSelectedItemPosition(),
                        priceSpinner.getSelectedItemPosition()
                );
            });
            binding.listViewItemMenu.setAdapter(itemAdapter);
        }
    }

    private void loadWishlist() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Boolean> wishlist = (Map<String, Boolean>) documentSnapshot.get("wishlist");
                if (wishlist != null) {
                    wishlistIds.addAll(wishlist.keySet());
                }
            }
        });
    }


//    private void filterFoods(int locationId, int timeSort, int priceSort) {
//        DatabaseReference foodsRef = database.getReference("Foods");
//
//        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<Foods> filteredFoods = new ArrayList<>();
//
//                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
//                    Foods food = foodSnapshot.getValue(Foods.class);
//                    if (food != null) {
//                        // Chỉ lọc các món ăn có locationId là 0 hoặc 1
//                        if (food.getLocationId() == locationId) {
//                            filteredFoods.add(food);
//                        }
//                    }
//                }
//
//                // Sắp xếp theo Time
//                if (timeSort == 2) {
//                    Collections.sort(filteredFoods, Comparator.comparingInt(Foods::getTimeValue));
//                } else if (timeSort == 1) {
//                    Collections.sort(filteredFoods, (f1, f2) -> Integer.compare(f2.getTimeValue(), f1.getTimeValue()));
//                }
//
//                // Sắp xếp theo Price
//                if (priceSort == 2) {
//                    Collections.sort(filteredFoods, Comparator.comparingDouble(Foods::getPrice));
//                } else if (priceSort == 1) {
//                    Collections.sort(filteredFoods, (f1, f2) -> Double.compare(f2.getPrice(), f1.getPrice()));
//                }
//
//                if (filteredFoods.isEmpty()) {
//                    Toast.makeText(MenuActivity.this, "Không có món ăn phù hợp", Toast.LENGTH_SHORT).show();
//                }
//
//                // ❗ Dùng adapter cho ListView chứ không phải RecyclerView
//                itemAdapter = new Item_Menu(filteredFoods, MenuActivity.this);
//                binding.listViewItemMenu.setAdapter(itemAdapter);
//                itemAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MenuActivity.this, "Lỗi khi tải món ăn", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}