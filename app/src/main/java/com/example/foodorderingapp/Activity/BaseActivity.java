package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class BaseActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    public String TAG = "foodapp";

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNav();
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup fullView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout contentFrame = fullView.findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, contentFrame, true);  // Inflate layout của Activity con
        super.setContentView(fullView);  // Set content view của BaseActivity
    }

    public void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        Log.d(TAG, "BottomNav is null: " + (bottomNav == null)); // Thêm dòng này
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_home && !(this instanceof MainActivity)) {
                    intent = new Intent(this, MainActivity.class);
                } else if (id == R.id.nav_menu && !(this instanceof MenuActivity)) {
                    intent = new Intent(this, MenuActivity.class);
                } else if (id == R.id.nav_order && !(this instanceof OrderActivity)) {
                    intent = new Intent(this, OrderActivity.class);
                } else if (id == R.id.nav_account && !(this instanceof AccountActivity)) {
                    intent = new Intent(this, AccountActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0); // không animation
                }

                return true;
            });

            // Chọn mục hiện tại
            if (this instanceof MainActivity) bottomNav.setSelectedItemId(R.id.nav_home);
            else if (this instanceof MenuActivity) bottomNav.setSelectedItemId(R.id.nav_menu);
            else if (this instanceof OrderActivity) bottomNav.setSelectedItemId(R.id.nav_order);
            else if (this instanceof AccountActivity) bottomNav.setSelectedItemId(R.id.nav_account);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

        setupBottomNav();
    }

    protected void setContentViewInBase(View childView) {
        FrameLayout frameLayout = findViewById(R.id.content_frame);
        if (frameLayout != null) {
            frameLayout.removeAllViews(); // clear nếu có sẵn
            frameLayout.addView(childView);
        }
    }
}