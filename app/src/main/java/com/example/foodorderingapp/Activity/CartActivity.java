package com.example.foodorderingapp.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.Adapter.CartAdapter;
import com.example.foodorderingapp.Domain.Address;
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
import java.util.Map;
import java.util.UUID;

public class CartActivity extends BaseActivity {


    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;
    private ActivityResultLauncher<Intent> addressLauncher;
    private ActivityResultLauncher<Intent> orderLauncher;

    private Address addressa=new Address();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // ⚠️ Di chuyển lên trên, trước khi thao tác với view

        // 1. Nhận địa chỉ nếu được gửi qua
        Address receivedAddress = (Address) getIntent().getSerializableExtra("selected_address");
        if (receivedAddress != null) {
            Log.d("CartActivity", "Received address ID: " + receivedAddress.getAddressId());
            addressa = receivedAddress;
            String addressName = addressa.getAddressName();
            String address = addressa.getAddress();
            String note = addressa.getNote();

            binding.txtViewAddressNameViewholderAddress.setText(addressName);
            binding.txtViewAddressViewholderAddress.setText(address);
            binding.txtViewAddressBox.setText(note);
        } else {
            loadDefaultAddress();
        }

        // 2. Đăng ký launcher để nhận kết quả từ AddressOrderActivity
        addressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Address receivedAddressResult = (Address) data.getSerializableExtra("selected_address");
                            if (receivedAddressResult != null) {
                                addressa = receivedAddressResult;
                                binding.txtViewAddressNameViewholderAddress.setText(addressa.getAddressName());
                                binding.txtViewAddressViewholderAddress.setText(addressa.getAddress());
                                binding.txtViewAddressBox.setText(addressa.getNote());
                            }
                        }
                    }
                }
        );

        // 3. Đăng ký launcher cho OrderActivity để reload lại sau khi đặt hàng xong
        orderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        recreate(); // Load lại Activity sau khi đặt hàng
                    }
                }
        );

        // 4. Khởi tạo logic còn lại
        managmentCart = new ManagmentCart(this);
        setVariable();
        calculateCart();
        initList();

        // 5. Sự kiện bấm nút Đặt hàng
        binding.placeorderbutton.setOnClickListener(v -> orderact());

        // 6. Sự kiện chọn địa chỉ (dùng launcher)
        binding.txtViewEditViewholderAddress.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, AddressOrderActivity.class);
            addressLauncher.launch(intent); // ⚠️ Phải dùng launch thay vì startActivity
        });
    }


    private void loadDefaultAddress() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> addressesMap = (Map<String, Object>) documentSnapshot.get("addresses");

                        if (addressesMap != null) {
                            for (Map.Entry<String, Object> entry : addressesMap.entrySet()) {
                                Map<String, Object> addressData = (Map<String, Object>) entry.getValue();

                                boolean isDefault = addressData.get("isDefault") != null && (boolean) addressData.get("isDefault");
                                if (isDefault) {
                                    // Lấy thông tin địa chỉ
                                    String addressName = (String) addressData.get("addressName");
                                    String address = (String) addressData.get("address");
                                    String note = (String) addressData.get("note");


                                    // Gán vào layout qua binding

                                    binding.txtViewAddressNameViewholderAddress.setText(addressName);
                                    binding.txtViewAddressViewholderAddress.setText(address);
                                    binding.txtViewAddressBox.setVisibility(View.VISIBLE);
                                    binding.txtViewAddressBox.setText(note);
                                    addressa.setAddress(address);
                                    addressa.setAddressName(addressName);
                                    addressa.setNote(note);


                                    return; // Chỉ cần 1 địa chỉ mặc định
                                }
                            }

                            // Không có địa chỉ mặc định
                            binding.txtViewAddressNameViewholderAddress.setText("No address found");
                            binding.txtViewAddressBox.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void orderact() {
        if(managmentCart.getListCart().isEmpty()){
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            binding.layoutOrderSummaryCartActivity.setVisibility(View.GONE);
            binding.placeorderbutton.setVisibility(View.GONE);
            binding.emptyTxt.setVisibility(View.VISIBLE);
            return;
        }

        // Kiểm tra địa chỉ đã chọn chưa
        if (addressa.getAddress() == null || addressa.getAddress().trim().isEmpty()) {
            showCustomAddAddressDialog(); // Hiển thị custom dialog
            return; // Không thực hiện đặt hàng nếu chưa có địa chỉ
        }

//            binding.layoutOrderSummaryCartActivity.setVisibility(View.VISIBLE);
//            binding.placeorderbutton.setVisibility(View.VISIBLE);
//            ArrayList<Foods> list=managmentCart.getListCart();
//            ArrayList<orderlist> orderlists=new ArrayList<>();
//            for(int i=0;i<list.size();i++){
//                orderlists.add(new orderlist(list.get(i).getTitle(),list.get(i).getNumberInCart(),list.get(i).getId()));
//            }
//            Order order=new Order();
//            order.setOrderlists(orderlists);
//            order.setStatus("DELIVERING");
//            double percentTax=0.02;
//            double delivery=10;
//            tax=Math.round((managmentCart.getTotalFee()*percentTax)*100.0)/100;
//            double total=Math.round((managmentCart.getTotalFee()+tax+delivery)*100)/100;
//            order.setTotal(total);
//            SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
//            SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
//
//            Date now = com.google.firebase.Timestamp.now().toDate();
//            order.setDate(sdfDate.format(now));
//            order.setTime(sdfTime.format(now));
//            if (addressa == null) {
//                Toast.makeText(getApplicationContext(), "Chưa thêm địa chỉ", Toast.LENGTH_SHORT).show();
//            } else {
//                order.setAddress(addressa);
//            }
//
//
//
//            String orderId = UUID.randomUUID().toString();
//            order.setOrderid(orderId);
//
//            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//            FirebaseFirestore.getInstance()
//                    .collection("users")
//                    .document(userId)
//                    .collection("orders")
//                    .document(orderId)
//                    .set(order)
//                    .addOnSuccessListener(aVoid -> {
//                        Log.d("Firebase", "Order added successfully");
//                        Toast.makeText(this, "Your order has been placed successfully!", Toast.LENGTH_SHORT).show();
//                        binding.cardView.removeAllViews();
//                         Intent intent=new Intent(CartActivity.this, OrderActivity.class);
//                         startActivity(intent);
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("Firebase", "Failed to add order", e);
//                    });
//
//
//        managmentCart.removecart();

        binding.layoutOrderSummaryCartActivity.setVisibility(View.VISIBLE);
        binding.placeorderbutton.setVisibility(View.VISIBLE);

        ArrayList<Foods> list = managmentCart.getListCart();
        ArrayList<orderlist> orderlists = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            orderlists.add(new orderlist(list.get(i).getTitle(), list.get(i).getNumberInCart(), list.get(i).getId()));
        }

        Order order = new Order();
        order.setOrderlists(orderlists);
        order.setStatus("DELIVERING");

        double percentTax = 0.02;
        double delivery = 10;
        tax = Math.round((managmentCart.getTotalFee() * percentTax) * 100.0) / 100;
        double total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100) / 100;
        order.setTotal(total);

        SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        Date now = com.google.firebase.Timestamp.now().toDate();
        order.setDate(sdfDate.format(now));
        order.setTime(sdfTime.format(now));

        // Đã chắc chắn có địa chỉ hợp lệ
        order.setAddress(addressa);

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
                    Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                    orderLauncher.launch(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to add order", e);
                });

        managmentCart.removecart();
        initList();
    }
    private void initList(){
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.cardView.setVisibility(View.GONE);
            binding.placeorderbutton.setVisibility(View.GONE);
            binding.layoutOrderSummaryCartActivity.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.cardView.setVisibility(View.VISIBLE);
            binding.placeorderbutton.setVisibility(View.VISIBLE);
            binding.layoutOrderSummaryCartActivity.setVisibility(View.VISIBLE);
        }
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cardView.setLayoutManager(linearLayoutManager);
        adapter=new CartAdapter(managmentCart.getListCart(),this,()->calculateCart());
        binding.cardView.setAdapter(adapter);


    }
    private void calculateCart(){
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

    private void showCustomAddAddressDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_addresswarning_cart);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        androidx.appcompat.widget.AppCompatButton btnCancel = dialog.findViewById(R.id.btnCancel);
        androidx.appcompat.widget.AppCompatButton btnAddAddress = dialog.findViewById(R.id.btnAddAddress);

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, AddressOrderActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}