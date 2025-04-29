package com.example.foodorderingapp.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.Domain.Address;
import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityEditAddressBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditAddressActivity extends AppCompatActivity {

    private ActivityEditAddressBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Address address = (Address) getIntent().getSerializableExtra("object");
        String addressId = address.getAddressId();  // đây chính là id cần để update trong Firebase
        String addressName = address.getAddressName();
        String addressDetail = address.getAddress();
        String note = address.getNote();
        boolean isDefault = address.isDefault();

        binding.editTxtAdrressNameEditAddressActivity.setText(addressName);
        binding.editTxtAdrressEditAddressActivity.setText(addressDetail);
        binding.editTxtNoteEditAddressActivity.setText(note);
        if (isDefault) {
            binding.checkboxEditAddressActivity.setChecked(true);
        } else {
            binding.checkboxEditAddressActivity.setChecked(false);
        }

        binding.btnDeleteEditAddressActivity.setOnClickListener(v -> {
            showDeleteAddressDialog(addressId);
        });



        binding.btnSaveEditAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("EditAddressActivity", "Save button clicked!");

                String addressName = binding.editTxtAdrressNameEditAddressActivity.getText().toString().trim();
                String addressDetail = binding.editTxtAdrressEditAddressActivity.getText().toString().trim();
                String note = binding.editTxtNoteEditAddressActivity.getText().toString().trim();
                boolean isDefault = binding.checkboxEditAddressActivity.isChecked();

                if (addressName.isEmpty()) {
                    Toast.makeText(EditAddressActivity.this, "Address name is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (addressDetail.isEmpty()) {
                    Toast.makeText(EditAddressActivity.this, "Address is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo đối tượng address mới
                Address newAddress = new Address(addressId, addressName, addressDetail, note, isDefault);

                // Update Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference userRef = db.collection("Users").document(userId);

                // Build Map cho address mới
                Map<String, Object> addressMap = new HashMap<>();
                addressMap.put("addressName", addressName);
                addressMap.put("address", addressDetail);
                addressMap.put("note", note);
                addressMap.put("isDefault", isDefault);

                // Build Map update
                Map<String, Object> updates = new HashMap<>();
                updates.put("addresses." + addressId, addressMap);

                userRef.update(updates)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(EditAddressActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                            // Gửi kết quả về AddressActivity
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updatedAddress", newAddress);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditAddressActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("UpdateError", "Failed to update address", e);
                        });
            }
        });


        binding.imgViewBackEditAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showDeleteAddressDialog(String addressId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_delete_address); // Layout của bạn
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Nền trong suốt

        AppCompatButton btnDecline = dialog.findViewById(R.id.btnDecline_DeleteAddress);
        AppCompatButton btnDelete = dialog.findViewById(R.id.btnDelete_DeleteAddress);

        btnDecline.setOnClickListener(v -> {
            dialog.dismiss(); // Đóng dialog
        });

        btnDelete.setOnClickListener(v -> {
            // Xử lý xoá address
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("addresses." + addressId, FieldValue.delete()); // Xoá địa chỉ

            userRef.update(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(EditAddressActivity.this, "Address deleted successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Báo cho AddressActivity biết cần reload
                        finish(); // Đóng EditAddressActivity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditAddressActivity.this, "Failed to delete address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            dialog.dismiss(); // Đóng dialog
        });

        dialog.show();
    }


}