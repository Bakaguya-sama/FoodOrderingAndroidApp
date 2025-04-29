package com.example.foodorderingapp.Activity;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

        binding.imgViewBackAddingNewAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnSaveAddingNewAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressName = binding.editTxtAdrressNameAddingNewAddressActivity.getText().toString();
                String address = binding.editTxtAddressAddingNewAddressActivity.getText().toString();
                String note = binding.editTxtNoteAddingNewAddressActivity.getText().toString();
                boolean isDefault = binding.checkboxAddingNewAddressActivity.isChecked();

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

                // Tạo 1 Map để lưu địa chỉ mới
                Map<String, Object> newAddress = new HashMap<>();
                newAddress.put("addressName", addressName);
                newAddress.put("address", address);
                newAddress.put("note", note);
                newAddress.put("isDefault", isDefault);

                // Sinh 1 id cho địa chỉ mới
                String addressId = db.collection("Users").document(userId).collection("temp").document().getId(); // chỉ để lấy id random

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
        });

        binding.btnCancelAddingNewAddressActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}