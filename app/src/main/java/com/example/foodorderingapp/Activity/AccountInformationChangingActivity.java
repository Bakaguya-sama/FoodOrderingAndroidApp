package com.example.foodorderingapp.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderingapp.R;
import com.example.foodorderingapp.databinding.ActivityAccountInformationChangingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AccountInformationChangingActivity extends BaseActivity {
    private ActivityAccountInformationChangingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAccountInformationChangingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Adapter cho Location
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender, R.layout.spinner_account);
        genderAdapter.setDropDownViewResource(R.layout.account_spinner_dropdown);
        binding.spinnerGenderAccountInformationChangingActivity.setAdapter(genderAdapter);

        binding.editTxtDateOfBirthAccountInformationChangingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AccountInformationChangingActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Tháng bắt đầu từ 0 nên cộng thêm 1
                            String dob = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                            binding.editTxtDateOfBirthAccountInformationChangingActivity.setText(dob);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        binding.imgViewBackAccountInformationChangingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        binding.btnSaveAccountInformationChangingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = binding.editTxtFullnameAccountInformationChangingActivity.getText().toString();
                String phone = binding.editTxtPhoneAccountInformationChangingActivity.getText().toString();
                String dob = binding.editTxtDateOfBirthAccountInformationChangingActivity.getText().toString();
                String gender = binding.spinnerGenderAccountInformationChangingActivity.getSelectedItem().toString();

                if (fullname.isEmpty()) {
                    Toast.makeText(AccountInformationChangingActivity.this, "Vui lòng nhập đầy đủ họ tên", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone.isEmpty()) {
                    Toast.makeText(AccountInformationChangingActivity.this, "Vui lòng nhập đầy đủ số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (binding.spinnerGenderAccountInformationChangingActivity.getSelectedItemPosition() == 0) {
                    Toast.makeText(AccountInformationChangingActivity.this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Map<String, Object> updates = new HashMap<>();
                updates.put("name", fullname);
                updates.put("phone", phone);
                updates.put("dob", dob);
                updates.put("gender", gender);

                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AccountInformationChangingActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_LONG).show();
                            loadUserData();
                            setResult(RESULT_OK);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AccountInformationChangingActivity.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        loadUserData();
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
                        String phone = documentSnapshot.getString("phone");
                        String dob = documentSnapshot.getString("dob");
                        String gender = documentSnapshot.getString("gender");

                        // Set dữ liệu nếu không null, ngược lại để trống
                        binding.editTxtFullnameAccountInformationChangingActivity.setText(name != null ? name : "");
                        binding.editTxtPhoneAccountInformationChangingActivity.setText(phone != null ? phone : "");
                        binding.editTxtDateOfBirthAccountInformationChangingActivity.setText(dob != null ? dob : "");
                        binding.txtViewUsernameActivityAccount.setText((name == null || name.isEmpty()) ? "Unknown" : name);

                        // Set giới tính vào Spinner nếu hợp lệ
                        if (gender != null) {
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.spinnerGenderAccountInformationChangingActivity.getAdapter();
                            int position = adapter.getPosition(gender);
                            if (position >= 0) {
                                binding.spinnerGenderAccountInformationChangingActivity.setSelection(position);
                            } else {
                                binding.spinnerGenderAccountInformationChangingActivity.setSelection(0); // Chọn mặc định
                            }
                        } else {
                            binding.spinnerGenderAccountInformationChangingActivity.setSelection(0); // Chọn mặc định nếu null
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}