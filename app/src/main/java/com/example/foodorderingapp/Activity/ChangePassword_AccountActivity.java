package com.example.foodorderingapp.Activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAccountInformationChangingBinding;
import com.example.foodorderingapp.databinding.ActivityChangePasswordAccountBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword_AccountActivity extends BaseActivity {
    private ActivityChangePasswordAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityChangePasswordAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupPasswordToggle(findViewById(R.id.editTxt_CurrentPass_ChangePassword_AccountActivity), findViewById(R.id.eye_current));
        setupPasswordToggle(findViewById(R.id.editTxt_NewPass_ChangePassword_AccountActivity), findViewById(R.id.eye_new));
        setupPasswordToggle(findViewById(R.id.editTxt_ReEnterNewPass_ChangePassword_AccountActivity), findViewById(R.id.eye_reenter));

        binding.btnBackChangePasswordAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnContinueChangePasswordAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = binding.editTxtCurrentPassChangePasswordAccountActivity.getText().toString();
                String newPassword = binding.editTxtNewPassChangePasswordAccountActivity.getText().toString();
                String reEnterNewPassword = binding.editTxtReEnterNewPassChangePasswordAccountActivity.getText().toString();

                if (currentPassword.isEmpty() || newPassword.isEmpty() || reEnterNewPassword.isEmpty()) {
                    Toast.makeText(ChangePassword_AccountActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(reEnterNewPassword)) {
                    Toast.makeText(ChangePassword_AccountActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                        user.reauthenticate(credential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(ChangePassword_AccountActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ChangePassword_AccountActivity.this, "Password update failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(ChangePassword_AccountActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

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


}