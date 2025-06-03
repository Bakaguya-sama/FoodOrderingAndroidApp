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

import android.location.LocationManager;
import android.provider.Settings;
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        checkIfLocationIsEnabled();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Kiểm tra xem định vị có được bật chưa
        checkIfLocationIsEnabled();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        androidx.appcompat.widget.AppCompatButton btnAddAddress = findViewById(R.id.btnAddAddress);
        tvAddress = findViewById(R.id.tvAddress);

        btnAddAddress.setOnClickListener(v -> {
            if (!currentAddress.isEmpty()) {

                new AlertDialog.Builder(MapActivity.this)
                        .setTitle("Address Confirmation")
                        .setMessage("Do you want to confirm this address?\n\n" + currentAddress)
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("address", currentAddress);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Unable to determine the address", Toast.LENGTH_SHORT).show();
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

//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//            if (location != null) {
//                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Vị trí hiện tại"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
//
//                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(
//                            location.getLatitude(), location.getLongitude(), 1);
//                    if (addresses != null && !addresses.isEmpty()) {
//                        currentAddress = addresses.get(0).getAddressLine(0);
//                        tvAddress.setText(currentAddress);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    tvAddress.setText("Error retrieving current address");
//                }
//            } else {
//                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
//            }
//        });

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Xóa marker cũ nếu có
                mMap.clear();

                // ✅ Thêm marker tại vị trí hiện tại
                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Vị trí hiện tại"));

                // ✅ Di chuyển camera đến vị trí hiện tại
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                // ✅ Lấy địa chỉ từ LatLng
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
                    tvAddress.setText("Error retrieving current address");
                }
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        });


        mMap.setOnMyLocationButtonClickListener(() -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Xóa các marker cũ
                    mMap.clear();

                    // ✅ Đặt marker màu đỏ
                    mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("Vị trí hiện tại"));

                    // ✅ Di chuyển camera đến đó (tuỳ chọn)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                    // ✅ Lấy địa chỉ
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
                        tvAddress.setText("Không thể lấy địa chỉ");
                    }
                }
            });

            return false; // Cho Google xử lý tiếp việc di chuyển camera
        });


        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected location"));
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
                    tvAddress.setText("Address not found");
                }
            } catch (IOException e) {
                e.printStackTrace();
                currentAddress = "";
                tvAddress.setText("Error retrieving address");
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
            Toast.makeText(this, "You need to grant location permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfLocationIsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location")
                    .setMessage("You need to enable location services to use the map. Do you want to open settings?")
                    .setPositiveButton("Open settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

}
