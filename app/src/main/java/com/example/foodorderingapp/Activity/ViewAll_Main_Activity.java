package com.example.foodorderingapp.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.Adapter.FoodListAdapter;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.R;

import java.util.ArrayList;

public class ViewAll_Main_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_all_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.back_ViewAll_Btn).setOnClickListener(v -> {
            finish(); // Đóng activity và quay lại màn hình trước
        });

        ArrayList<Foods> bestFoodList = (ArrayList<Foods>) getIntent().getSerializableExtra("bestFoodList");

        // ví dụ: set adapter với RecyclerView
        RecyclerView recyclerView = findViewById(R.id.bestfoodList_RecycleView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new FoodListAdapter(bestFoodList));
    }
}