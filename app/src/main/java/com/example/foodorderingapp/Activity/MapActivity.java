package com.example.foodorderingapp.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderingapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLatLng;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private String currentAddress = "";
    TextView tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btnAddAddress = findViewById(R.id.btnAddAddress);
         tvAddress = findViewById(R.id.tvAddress);

        btnAddAddress.setOnClickListener(v -> {
            if (!currentAddress.isEmpty()) {

                new AlertDialog.Builder(MapActivity.this)
                        .setTitle("Xác nhận địa chỉ")
                        .setMessage("Bạn có muốn xác nhận địa chỉ này không?\n\n" + currentAddress)
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("address", currentAddress);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                Toast.makeText(this, "Không xác định được địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null && mapFragment.getView() != null) {
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                    .findViewById(Integer.parseInt("2"));
            if (locationButton != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

                // Xóa rule mặc định ở dưới
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                // Đổi vị trí sang góc trên bên phải, cách trên 80px và cách phải 10px
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.setMargins(0, 250, 10, 0);

                locationButton.setLayoutParams(params);
            }
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Vị trí hiện tại"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        currentAddress = addresses.get(0).getAddressLine(0);
                        tvAddress.setText(currentAddress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    tvAddress.setText("Không thể lấy địa chỉ hiện tại");
                }
            } else {
                Toast.makeText(this, "Không tìm được vị trí", Toast.LENGTH_SHORT).show();
            }
        });


        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí bạn chọn"));
            currentLatLng = latLng;

            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    currentAddress = addresses.get(0).getAddressLine(0);
                    tvAddress.setText(currentAddress);
                } else {
                    currentAddress = "";
                    tvAddress.setText("Không tìm thấy địa chỉ");
                }
            } catch (IOException e) {
                e.printStackTrace();
                currentAddress = "";
                tvAddress.setText("Lỗi khi lấy địa chỉ");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap); // Gọi lại nếu được cấp quyền
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền vị trí", Toast.LENGTH_SHORT).show();
        }
    }
}
