package com.example.foodorderingapp.Activity;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.example.foodorderingapp.databinding.ActivityAccountBinding;
import com.example.foodorderingapp.databinding.ActivityDetailBinding;
import com.example.foodorderingapp.databinding.ActivityMainBinding;
import com.example.foodorderingapp.databinding.ActivityOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

//        binding= ActivityOrderBinding.inflate(getLayoutInflater());
//        super.setContentView(R.layout.activity_order);

        binding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentViewInBase(binding.getRoot());
        setupBottomNav();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        initList();



    }
    private void deleteAllUserOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy reference đến collection orders của user
        CollectionReference ordersRef = db.collection("users")
                .document(currentUserId)
                .collection("orders");

        // Lấy tất cả documents trong collection orders
        ordersRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Tạo một batch delete
                    WriteBatch batch = db.batch();

                    // Thêm tất cả documents vào batch delete
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }

                    // Thực hiện batch delete
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                // Xóa thành công
                                Toast.makeText(this, "All orders deleted successfully", Toast.LENGTH_SHORT).show();

                                // Cập nhật UI
                                orderList.clear();
                                adapter.notifyDataSetChanged();

                                binding.orderlist.setVisibility(View.GONE);
                                binding.textView24.setVisibility(View.VISIBLE);
                                binding.imageView5.setVisibility(View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                // Xóa thất bại
                                Toast.makeText(this, "Failed to delete orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Error deleting orders", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firebase", "Error getting orders", e);
                });
    }
    private void initList() {
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, orderList);
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

                    if (!orderList.isEmpty()) {
                        binding.orderlist.setVisibility(View.VISIBLE);
                        binding.imageView5.setVisibility(View.GONE);
                        binding.textView24.setVisibility(View.GONE);
                    } else {
                        binding.orderlist.setVisibility(View.GONE);
                        binding.textView24.setVisibility(View.VISIBLE);
                        binding.imageView5.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to load orders", e);
                });
    }

}