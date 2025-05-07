package com.example.foodorderingapp.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAccountBinding;
import com.example.foodorderingapp.databinding.ActivityCartBinding;
import com.example.foodorderingapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends BaseActivity {
    private ActivityAccountBinding binding;
    private ActivityResultLauncher<Intent> accountInfoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

//        binding= ActivityAccountBinding.inflate(getLayoutInflater());
////        super.setContentView(R.layout.activity_account);
//        setContentView(binding.getRoot());

        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentViewInBase(binding.getRoot());
        setupBottomNav();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//            if (id == R.id.nav_home) {
//                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            else if (id == R.id.nav_menu) {
//                Intent intent = new Intent(AccountActivity.this, MenuActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            else if (id == R.id.nav_order) {
//
//                return true;
//            }
//            else if (id == R.id.nav_account) {
//                Intent intent = new Intent(AccountActivity.this, AccountActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            return false;
//        });

        binding.imgViewWishlistAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, Wishlist_AccountActivity.class);
                startActivity(intent);
            }
        });

        binding.imgViewAccountInformationAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountInfoLauncher.launch(new Intent(AccountActivity.this, AccountInformationChangingActivity.class));
            }
        });

        binding.imgViewAddressAccountAcitvity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, AddressActivity.class);
                startActivity(intent);
            }
        });

        binding.imgViewBackAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.txtViewChangePassAccountAcitvity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ChangePassword_AccountActivity.class);
                startActivity(intent);
            }
        });

        binding.txtViewDeleteAccountAccountAcitvity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDeleteDialog();
            }
        });

        binding.txtViewLogOutAccountAcitvity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSignOutDialog();
            }
        });


        binding.imgViewOrderHistoryAccountAcitvity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AccountActivity.this,OrderActivity.class);
                startActivity(intent);
            }
        });

        loadUserData();

        accountInfoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Khi AccountInformationChangingActivity cập nhật xong và trả kết quả OK
                        loadUserData(); // GỌI LẠI load user mới
                    }
                }
        );


    }
    private void showCustomDeleteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_delete_account);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        androidx.appcompat.widget.AppCompatButton btnAgree = dialog.findViewById(R.id.btnAgree_DeleteAccount);
        androidx.appcompat.widget.AppCompatButton btnSkip = dialog.findViewById(R.id.btnSkip_DeleteAccount);

        btnAgree.setOnClickListener(v -> {
            // Xử lý khi người dùng đồng ý xoá tài khoản
            deleteAccount(dialog);
            dialog.dismiss();
        });

        btnSkip.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteAccount(Dialog dialog) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Bước 1: Xoá document trong Firestore
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Bước 2: Xoá tài khoản trong Authentication
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Tài khoản đã được xóa", Toast.LENGTH_SHORT).show();
                                        auth.signOut();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Xóa tài khoản thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi xoá dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void showCustomSignOutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_logout_account);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        androidx.appcompat.widget.AppCompatButton btnAgree = dialog.findViewById(R.id.btnAgree_SignOut);
        androidx.appcompat.widget.AppCompatButton btnSkip = dialog.findViewById(R.id.btnSkip_SignOut);

        btnAgree.setOnClickListener(v -> {
            signOut(dialog);
            dialog.dismiss();
        });

        btnSkip.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void signOut(Dialog dialog) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Đăng xuất người dùng hiện tại
        auth.signOut();

        // Thông báo cho người dùng đã đăng xuất thành công
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Chuyển hướng người dùng đến màn hình đăng nhập
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");

                        // Set dữ liệu nếu không null, ngược lại để trống
//                        binding.txtViewUsernameActivityAccount.setText(name != null ? name : "Unknown");
                        binding.txtViewUsernameActivityAccount.setText((name == null || name.isEmpty()) ? "Unknown" : name);

                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}