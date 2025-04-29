package com.example.foodorderingapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends BaseActivity {

    ActivitySignupBinding binding;
    private EditText emailEdit, passEdit, reEnterPassEdit;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        emailEdit = findViewById(R.id.emailEdit);
        passEdit = findViewById(R.id.passEdit);
        reEnterPassEdit = findViewById(R.id.reEnterPassEdit);

        setupPasswordToggle(findViewById(R.id.passEdit), findViewById(R.id.eye_pass_signup));
        setupPasswordToggle(findViewById(R.id.reEnterPassEdit), findViewById(R.id.eye_reenterpass_signup));

        setVariable();
        TextView textView = findViewById(R.id.textView5);
        String fullText = "Are you a member? Login";
        SpannableString spannableString = new SpannableString(fullText);

        // Vị trí bắt đầu và kết thúc của chữ "Login"
        int start = fullText.indexOf("Login");
        int end = start + "Login".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupPasswordToggle(EditText editText, ImageView eyeIcon) {
        final boolean[] isVisible = {false};
        final Typeface originalTypeface = editText.getTypeface(); // Lưu lại font gốc

        eyeIcon.setOnClickListener(v -> {
            if (isVisible[0]) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.visibility_off);
            } else {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.visibility_on);
            }

            // Restore font sau khi thay đổi inputType
            editText.setTypeface(originalTypeface);
            editText.setSelection(editText.getText().length());

            isVisible[0] = !isVisible[0];
        });
    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(v -> {
            String email = binding.emailEdit.getText().toString();
            String pass = binding.passEdit.getText().toString();
            String reEnterPass = binding.reEnterPassEdit.getText().toString();

            // Validate input
            if (!validateInput(email, pass, reEnterPass)) return;

            // Tạo tài khoản với Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Tạo document user trong Firestore
                                createFirestoreUser(user.getUid(), email);
                            }
//                            navigateToMain(); // ✅ chuyển sang Main chỉ khi đã lưu xong
                        } else {
                            handleError(task.getException());
                        }
                    });
        });
    }

    // Kiểm tra dữ liệu đầu vào
    private boolean validateInput(String email, String pass, String reEnterPass) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!pass.equals(reEnterPass)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Tạo user trong Firestore
    private void createFirestoreUser(String userId, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", "");
        user.put("phone", "");
        user.put("addresses", new HashMap<>());
        user.put("wishlist", new HashMap<>());
        user.put("orders", new HashMap<>());

        db.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created in Firestore");
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user", e);
                    Toast.makeText(this, "Lỗi khi tạo tài khoản trên Firestore", Toast.LENGTH_SHORT).show();
                });
    }


    // Chuyển đến màn hình chính
    private void navigateToMain() {
        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Xử lý lỗi
    private void handleError(Exception exception) {
        Log.e(TAG, "Đăng ký thất bại: " + exception.getMessage());
        String errorMessage = "Đăng ký thất bại: " + exception.getMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}