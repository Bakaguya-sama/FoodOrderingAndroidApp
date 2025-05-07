package com.example.foodorderingapp.Activity;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.Adapter.CartAdapter;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.Domain.Order;
import com.example.foodorderingapp.Domain.orderlist;
import com.example.foodorderingapp.Helper.ManagmentCart;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CartActivity extends BaseActivity {


    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCartBinding.inflate(getLayoutInflater());


       setContentView(binding.getRoot());
       managmentCart=new ManagmentCart(this);
       setVariable();
       calculateCart();
       initList();

       binding.placeorderbutton.setOnClickListener(v -> orderact());
    }

    private void orderact() {
        if(managmentCart.getListCart().isEmpty()){
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();


        }
        else{
            ArrayList<Foods> list=managmentCart.getListCart();
            ArrayList<orderlist> orderlists=new ArrayList<>();
            for(int i=0;i<list.size();i++){
                orderlists.add(new orderlist(list.get(i).getTitle(),list.get(i).getNumberInCart(),list.get(i).getId()));
            }
            Order order=new Order();
            order.setOrderlists(orderlists);
            double percentTax=0.02;
            double delivery=10;
            tax=Math.round((managmentCart.getTotalFee()*percentTax)*100.0)/100;
            double total=Math.round((managmentCart.getTotalFee()+tax+delivery)*100)/100;
            order.setTotal(total);
            SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

            Date now = com.google.firebase.Timestamp.now().toDate();
            order.setDate(sdfDate.format(now));
            order.setTime(sdfTime.format(now));

            String orderId = UUID.randomUUID().toString();
            order.setOrderid(orderId);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("orders")
                    .document(orderId)
                    .set(order)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Order added successfully");
                        Toast.makeText(this, "Your order has been placed successfully!", Toast.LENGTH_SHORT).show();
                        binding.cardView.removeAllViews();
                        finish();


                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to add order", e);
                    });


        }
        managmentCart.removecart();

    }
    private void initList(){
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewcart.setVisibility(View.GONE);

        }
        else{
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewcart.setVisibility(View.VISIBLE);

        }
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cardView.setLayoutManager(linearLayoutManager);
        adapter=new CartAdapter(managmentCart.getListCart(),this,()->calculateCart());
        binding.cardView.setAdapter(adapter);


    }
    private void    calculateCart(){
        double percentTax=0.02;
        double delivery=10;
       tax=Math.round((managmentCart.getTotalFee()*percentTax)*100.0)/100;
       double total=Math.round((managmentCart.getTotalFee()+tax+delivery)*100)/100;
       double itemtotal=Math.round(managmentCart.getTotalFee()*100)/100;

       binding.totalFeeTxt.setText("$"+itemtotal);
       binding.taxTxt.setText("$"+tax);
       binding.deliveryTxt.setText("$"+delivery);
       binding.totalTxt.setText("$"+total);
    }
    private void setVariable(){
        binding.backBtn.setOnClickListener(v -> finish());
    }
}