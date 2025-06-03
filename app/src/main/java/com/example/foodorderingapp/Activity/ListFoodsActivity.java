package com.example.foodorderingapp.Activity;

import android.os.Bundle;
import android.view.View;
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

import com.example.foodorderingapp.Adapter.FoodListAdapter;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityListFoodsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFoodsActivity extends BaseActivity{
ActivityListFoodsBinding binding;
private RecyclerView.Adapter adapterListFood;
private int categoryId;
private String categoryName;
private String searchText;
private Boolean isSearch;
private ArrayList<Foods> filteredList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityListFoodsBinding.inflate(getLayoutInflater());


        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
        setVariable();
    }
    private void setVariable(){

    }


//    private void initList(){
//        DatabaseReference myRef=database.getReference("Foods");
//        binding.progressBar3.setVisibility(View.VISIBLE);
//        ArrayList<Foods> list=new ArrayList<>();
//        Query query;
//        if(isSearch){
//            query=myRef.orderByChild("Title").startAt(searchText).endAt(searchText+'\uf8ff');
//        }else{
//            query=myRef.orderByChild("CategoryId").equalTo(categoryId);
//
//        }
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if( snapshot.exists()){
//                    for(DataSnapshot issue:snapshot.getChildren()){
//                        list.add(issue.getValue(Foods.class));
//                    }
//                    if(list.size()>0){
//                        binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this,2));
//                        adapterListFood=new FoodListAdapter(list);
//                        binding.foodListView.setAdapter(adapterListFood);
//                    }
//                    else {
//                        binding.textView11.setVisibility(View.VISIBLE); // Hiện text nếu list rỗng
//                    }
//                } else {
//                    // Trường hợp snapshot KHÔNG tồn tại (không có dữ liệu)
//                    binding.textView11.setVisibility(View.VISIBLE);
//                }
//                binding.progressBar3.setVisibility(View.GONE); // LUÔN ẩn progressBar sau khi xử lý
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void initList(){
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar3.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        Query query;
        if (isSearch) {
            query = myRef; // Tải toàn bộ để lọc thủ công
        } else {
            query = myRef.orderByChild("CategoryId").equalTo(categoryId);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods food = issue.getValue(Foods.class);
                        if (food != null) {
                            list.add(food);
                        }
                    }

                    if (isSearch && searchText != null) {
                        String search = searchText.trim().toLowerCase();
                        for (Foods food : list) {
                            if (food.getTitle() != null) {
                                String title = food.getTitle().toLowerCase();
                                // Contains hoặc độ tương đồng >= 70%
                                if (title.contains(search) || similarity(search, title) > 0.7) {
                                    filteredList.add(food);
                                }
                            }
                        }
                    } else {
                        filteredList = list; // Không tìm kiếm, giữ nguyên
                    }

                    if (filteredList.size() > 0) {
                        binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this, 2));
                        adapterListFood = new FoodListAdapter(filteredList);
                        binding.foodListView.setAdapter(adapterListFood);
                        binding.textView11.setVisibility(View.GONE);
                    } else {
                        binding.textView11.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.textView11.setVisibility(View.VISIBLE);
                }
                binding.progressBar3.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar3.setVisibility(View.GONE);
                Toast.makeText(ListFoodsActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double similarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        return (maxLen - levenshtein(s1, s2)) / (double) maxLen;
    }

    private int levenshtein(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }


    private void getIntentExtra(){
        searchText = getIntent().getStringExtra("searchText");
        categoryId=getIntent().getIntExtra("CategoryId",0);
        categoryName=getIntent().getStringExtra("Category");
        isSearch=getIntent().getBooleanExtra("isSearch",false);
        binding.titleTxt.setText(categoryName);
        binding.backBtn.setOnClickListener(v->{
            finish();

        });

    }

}