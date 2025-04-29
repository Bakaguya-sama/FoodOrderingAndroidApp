package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
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
import com.example.foodorderingapp.Domain.Category;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.Domain.Location;
import com.example.foodorderingapp.Domain.Price;
import com.example.foodorderingapp.Domain.Time;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAccountBinding;
import com.example.foodorderingapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private TextView txtView_Username;
    private ArrayList<Foods> bestFoodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentViewInBase(binding.getRoot());
        setupBottomNav();
//        setContentView(binding.getRoot());
        // Thay vì setContentView(binding.getRoot())
        // Ta thêm vào FrameLayout trong activity_base.xml
//        FrameLayout contentFrame = findViewById(R.id.content_frame);
//        contentFrame.addView(binding.getRoot());
//        binding= ActivityMainBinding.inflate(getLayoutInflater());
//        super.setContentView(R.layout.activity_main);

//        txtView_Username = findViewById(R.id.txtView_Username);
//        String email = getIntent().getStringExtra("email");
//        txtView_Username.setText(email);
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            binding.txtViewUsername.setText(email);
        } else {
            // Có thể chuyển hướng về LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

//      setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.textViewViewAll.setOnClickListener(v -> onClick_ViewAll_Main(v));


//        initLocation();
//        initTime();
//        initPrice();
        initBestFood();
        initCategory();

        initBestFood();

        setVariable();
    }

    private void onClick_ViewAll_Main(View view) {
        Intent intent = new Intent(MainActivity.this, ViewAll_Main_Activity.class);
        intent.putExtra("bestFoodList", bestFoodList);
        startActivity(intent);
    }

    private void setVariable() {
        binding.filterBtn.setOnClickListener(v -> {
            // Gọi lại initBestFood() để chỉ hiện BestFood mà không filter gì
            initBestFood();

//            // Reset Spinner nếu bạn muốn
//            binding.locationSpinner.setSelection(0);
//            binding.timeSpinner.setSelection(0);
//            binding.priceSpinner.setSelection(0);

            Toast.makeText(MainActivity.this, "Đã hiển thị món ăn nổi bật", Toast.LENGTH_SHORT).show();
        });



        binding.logOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
        binding.searchBtn.setOnClickListener(v -> {
            String text = binding.searchEdt.getText().toString();
            if (!text.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);
                intent.putExtra("searchText", text);
                intent.putExtra("isSearch", true);
                startActivity(intent);
            }
        });
        binding.cartBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        });

//        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                triggerFilter();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        };

//        binding.locationSpinner.setOnItemSelectedListener(spinnerListener);
//        binding.timeSpinner.setOnItemSelectedListener(spinnerListener);
//        binding.priceSpinner.setOnItemSelectedListener(spinnerListener);
    }

    private void filterFoods(int locationId, int timeId, int priceId) {
        DatabaseReference foodsRef = database.getReference("Foods");
        Query query = foodsRef.orderByChild("BestFood").equalTo(true); // chỉ lấy món BestFood

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Foods> filteredFoods = new ArrayList<>();

                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    Foods food = foodSnapshot.getValue(Foods.class);
                    if (food != null) {
                        boolean matchLocation = (locationId == 0 || food.getLocationId() == locationId);
                        boolean matchTime = (timeId == 0 || food.getTimeId() == timeId);
                        boolean matchPrice = (priceId == 0 || food.getPriceId() == priceId);

                        if (matchLocation && matchTime && matchPrice) {
                            filteredFoods.add(food);
                        }
                    }
                }

                if (filteredFoods.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Không có món ăn phù hợp", Toast.LENGTH_SHORT).show();
                }

                RecyclerView.Adapter adapter = new BestFoodsAdapter(filteredFoods);
                binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                binding.bestFoodView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi khi tải món ăn", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean containsFood(ArrayList<Foods> list, Foods food) {
        for (Foods f : list) {
            if (f.getId() == food.getId()) return true;
        }
        return false;
    }


//    private void triggerFilter() {
//        Location selectedLocation = (Location) binding.locationSpinner.getSelectedItem();
//        Time selectedTime = (Time) binding.timeSpinner.getSelectedItem();
//        Price selectedPrice = (Price) binding.priceSpinner.getSelectedItem();
//
//        if (selectedLocation != null && selectedTime != null && selectedPrice != null) {
//            filterFoods(selectedLocation.getId(), selectedTime.getId(), selectedPrice.getId());
//        }
//    }
    private void initBestFood() {
        bestFoodList.clear();
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBarBestFood.setVisibility(View.VISIBLE);

        Query query = myRef.orderByChild("BestFood").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bestFoodList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);
                        if (food != null) {
                            bestFoodList.add(food);
                        }
                    }
                }

                binding.progressBarBestFood.setVisibility(View.GONE);

                if (!bestFoodList.isEmpty()) {
                    RecyclerView.Adapter adapter = new BestFoodsAdapter(bestFoodList);
                    binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    binding.bestFoodView.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Không có món ăn nổi bật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarBestFood.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi khi tải món ăn nổi bật", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Category.class));
                    }
                    if (list.size() > 0) {
                        binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
                        RecyclerView.Adapter adapter = new CategoryAdapter(list);
                        binding.categoryView.setAdapter(adapter);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void initLocation() {
//        DatabaseReference myRef = database.getReference("Location");
//        ArrayList<Location> list = new ArrayList<>();
//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot issue : snapshot.getChildren()) {
//                        list.add(issue.getValue(Location.class));
//                    }
//
//                    Location allLocation = new Location(0, "All");
//                    list.add(0, allLocation);
//
//                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    binding.locationSpinner.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void initTime() {
//        DatabaseReference myRef = database.getReference("Time");
//        ArrayList<Time> list = new ArrayList<>();
//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot issue : snapshot.getChildren()) {
//                        list.add(issue.getValue(Time.class));
//                    }
//                    Time allTime = new Time(0, "All");
//                    list.add(0, allTime);
//
//                    ArrayAdapter<Time> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    binding.timeSpinner.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void initPrice() {
//        DatabaseReference myRef = database.getReference("Price");
//        ArrayList<Price> list = new ArrayList<>();
//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot issue : snapshot.getChildren()) {
//                        list.add(issue.getValue(Price.class));
//                    }
//
//                    Price allPrice = new Price(0, "All");
//                    list.add(0, allPrice);
//
//                    ArrayAdapter<Price> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    binding.priceSpinner.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}