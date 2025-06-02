package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.Adapter.Address_Account_Activity;
import com.example.foodorderingapp.Domain.Address;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAddressBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class AddressActivity extends BaseActivity {

    private ActivityAddressBinding binding;
    private Address_Account_Activity addressAdapter;
    private ArrayList<Address> addressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.imgViewBackAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressActivity.this, AddingNewAddressActivity.class);
                startActivity(intent);
            }
        });

        // Trong AddressActivity.java
        binding.listViewAddressAddressActivity.setOnItemClickListener((parent, view, position, id) -> {
            Address selectedAddress = addressList.get(position);
            Intent intent = new Intent(AddressActivity.this, EditAddressActivity.class);
            intent.putExtra("object", selectedAddress);  // Gửi đối tượng địa chỉ cần chỉnh sửa
            startActivityForResult(intent, 1);  // Gọi EditAddressActivity và nhận kết quả
        });



        loadAddresses();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Address updatedAddress = (Address) data.getSerializableExtra("updatedAddress");  // Lấy địa chỉ đã được cập nhật
            if (updatedAddress != null) {
                // Cập nhật lại danh sách địa chỉ
                for (int i = 0; i < addressList.size(); i++) {
                    if (addressList.get(i).getAddressId().equals(updatedAddress.getAddressId())) {
                        addressList.set(i, updatedAddress);  // Cập nhật địa chỉ trong danh sách
                        break;
                    }
                }
                addressAdapter.notifyDataSetChanged();  // Thông báo adapter đã thay đổi
            }
        }
    }

    private void loadAddresses() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> addressesMap = (Map<String, Object>) documentSnapshot.get("addresses");
                        addressList.clear(); // Clear trước khi add mới

                        if (addressesMap != null) {
                            for (Map.Entry<String, Object> entry : addressesMap.entrySet()) {
                                String addressId = entry.getKey();
                                Map<String, Object> addressData = (Map<String, Object>) entry.getValue();

                                String addressName = (String) addressData.get("addressName");
                                String address = (String) addressData.get("address");
                                String note = (String) addressData.get("note");
                                boolean isDefault = addressData.get("isDefault") != null && (boolean) addressData.get("isDefault");

                                Address addr = new Address(addressId, addressName, address, note, isDefault);
                                addressList.add(addr);
                            }
                        }

                        if (!addressList.isEmpty()) {
                            binding.txtViewNoAddressAddressActivity.setVisibility(View.GONE);
                            binding.listViewAddressAddressActivity.setVisibility(View.VISIBLE);
                        } else {
                            binding.txtViewNoAddressAddressActivity.setVisibility(View.VISIBLE);
                            binding.listViewAddressAddressActivity.setVisibility(View.GONE);
                        }

                        if (addressAdapter == null) {
                            addressAdapter = new Address_Account_Activity(addressList, AddressActivity.this);
                            binding.listViewAddressAddressActivity.setAdapter(addressAdapter);
                        }
//                        else {
//                            addressAdapter.notifyDataSetChanged();
//                        }
                        addressAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();  // Load lại địa chỉ khi quay lại
    }

}