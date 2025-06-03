package com.example.foodorderingapp.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAccountInformationChangingBinding;
import com.example.foodorderingapp.databinding.ActivityAddingNewAddressBinding;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class AddingNewAddressActivity extends BaseActivity {

    private ActivityAddingNewAddressBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddingNewAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.editTxtAddressAddingNewAddressActivity.setFocusable(false);
        binding.editTxtAddressAddingNewAddressActivity.setClickable(true);

// Bắt sự kiện click để hiện thông báo
        binding.editTxtAddressAddingNewAddressActivity.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng chọn địa chỉ từ bản đồ", Toast.LENGTH_SHORT).show();
        });
//        Intent intent = new Intent(this, MapActivity.class);
//        startActivityForResult(intent, 100);

        binding.imgViewBackAddingNewAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.btnMap.setOnClickListener(V->{
            Intent intent=new Intent(AddingNewAddressActivity.this, MapActivity.class);
            startActivityForResult(intent, 100);
        });

        binding.btnSaveAddingNewAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressName = binding.editTxtAdrressNameAddingNewAddressActivity.getText().toString().trim();
                String address = binding.editTxtAddressAddingNewAddressActivity.getText().toString().trim();
                String note = binding.editTxtNoteAddingNewAddressActivity.getText().toString().trim();
                boolean isDefaultChecked = binding.checkboxAddingNewAddressActivity.isChecked();

                if (addressName.isEmpty()) {
                    Toast.makeText(AddingNewAddressActivity.this, "Please enter address name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (address.isEmpty()) {
                    Toast.makeText(AddingNewAddressActivity.this, "Please enter address", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("Users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            Map<String, Object> addresses = (Map<String, Object>) documentSnapshot.get("addresses");

                            boolean isFirstAddress = (addresses == null || addresses.isEmpty());
                            boolean finalIsDefault = isFirstAddress || isDefaultChecked;

                            // Nếu là mặc định, update tất cả các địa chỉ khác thành false
                            if (finalIsDefault && addresses != null && !addresses.isEmpty()) {
                                Map<String, Object> updates = new HashMap<>();
                                for (String key : addresses.keySet()) {
                                    Map<String, Object> addr = (Map<String, Object>) addresses.get(key);
                                    addr.put("isDefault", false);
                                    updates.put("addresses." + key, addr);
                                }

                                db.collection("Users").document(userId).update(updates)
                                        .addOnSuccessListener(aVoid -> addNewAddress(userId, addressName, address, note, finalIsDefault))
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AddingNewAddressActivity.this, "Error updating old addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });


                            } else {
                                // Không cần cập nhật địa chỉ cũ
                                addNewAddress(userId, addressName, address, note, finalIsDefault);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddingNewAddressActivity.this, "Failed to get existing addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            }
        });

        binding.btnCancelAddingNewAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    // Hàm tách riêng để thêm địa chỉ mới
    private void addNewAddress(String userId, String addressName, String address, String note, boolean isDefault) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> newAddress = new HashMap<>();
        newAddress.put("addressName", addressName);
        newAddress.put("address", address);
        newAddress.put("note", note);
        newAddress.put("isDefault", isDefault);

        String addressId = db.collection("Users").document(userId).collection("temp").document().getId();

        db.collection("Users")
                .document(userId)
                .update("addresses." + addressId, newAddress)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(AddingNewAddressActivity.this, "Address added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddingNewAddressActivity.this, "Failed to add address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            binding.editTxtAddressAddingNewAddressActivity.setText(address);
        }
    }

}