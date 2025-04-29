package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityEmailEnteringBinding;
import com.example.foodorderingapp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class EmailEnteringActivity extends BaseActivity {
    ActivityEmailEnteringBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEmailEnteringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.textViewResendEmailEmailEntering.setOnClickListener(v -> sendResetPasswordEmail());
    }
    public void onClick_Continue_EmailEntering(View view) {
        sendResetPasswordEmail();
    }

//    private void sendResetPasswordEmail() {
//        String email = binding.emailVerificationEnteringEmail.getText().toString().trim();
//
//        if (email.isEmpty()) {
//            binding.emailVerificationEnteringEmail.setError("Vui lòng nhập email");
//            return;
//        }
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//
//        auth.fetchSignInMethodsForEmail(email)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        boolean isRegistered = !task.getResult().getSignInMethods().isEmpty();
//                        if (isRegistered) {
//                            // Nếu email đã tồn tại, gửi email reset
//                            auth.sendPasswordResetEmail(email)
//                                    .addOnCompleteListener(resetTask -> {
//                                        if (resetTask.isSuccessful()) {
//                                            Toast.makeText(this, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
//
//                                            new Handler().postDelayed(() -> {
//                                                startActivity(new Intent(this, LoginActivity.class));
//                                                finish();
//                                            }, 3000); // 3 giây
//                                        } else {
//                                            Toast.makeText(this, "Không thể gửi email. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        } else {
//                            // Email chưa được đăng ký
//                            Toast.makeText(this, "Email này chưa được đăng ký tài khoản.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(this, "Lỗi kiểm tra email. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private void sendResetPasswordEmail() {
//        String email = binding.emailVerificationEnteringEmail.getText().toString().trim();
//
//        if (email.isEmpty()) {
//            binding.emailVerificationEnteringEmail.setError("Vui lòng nhập email");
//            return;
//        }
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//
//        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                List<String> signInMethods = task.getResult().getSignInMethods();
//
//                if (signInMethods != null && !signInMethods.isEmpty()) {
//                    Log.d("EmailCheck", "Sign-in methods for " + email + ": " + signInMethods);
//
//                    if (signInMethods.contains("password")) {
//                        auth.sendPasswordResetEmail(email)
//                                .addOnCompleteListener(resetTask -> {
//                                    if (resetTask.isSuccessful()) {
//                                        Toast.makeText(this, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
//                                        new Handler().postDelayed(() -> {
//                                            startActivity(new Intent(this, LoginActivity.class));
//                                            finish();
//                                        }, 3000);
//                                    } else {
//                                        Toast.makeText(this, "Không thể gửi email. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    } else {
//                        Toast.makeText(this, "Email này được đăng ký bằng Google hoặc phương thức khác.", Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    Log.d("EmailCheck", "No sign-in method for email: " + email);
//                    Toast.makeText(this, "Email này chưa được đăng ký tài khoản.", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Log.e("EmailCheck", "Lỗi khi kiểm tra email", task.getException());
//                Toast.makeText(this, "Lỗi kiểm tra email. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void sendResetPasswordEmail() {
        String email = binding.emailVerificationEnteringEmail.getText().toString().trim();

        if (email.isEmpty()) {
            binding.emailVerificationEnteringEmail.setError("Vui lòng nhập email");
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(resetTask -> {
                    if (resetTask.isSuccessful()) {
                        Toast.makeText(this, "Nếu địa chỉ email tồn tại trong hệ thống, bạn sẽ nhận được email đặt lại mật khẩu.", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }, 3000);
                    } else {
                        //Log lỗi cụ thể để gỡ lỗi, nhưng không hiển thị cho người dùng
                        Log.e("PasswordReset", "Không thể gửi email đặt lại mật khẩu", resetTask.getException());
                        Toast.makeText(this, "Có lỗi xảy ra. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}