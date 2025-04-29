package com.example.foodorderingapp.Domain;

import java.io.Serializable;

public class Address implements Serializable {

    private String addressId;   // ID của địa chỉ
    private String addressName; // Tên địa chỉ (ví dụ: "Nhà", "Công ty")
    private String address;     // Chi tiết địa chỉ (ví dụ: "123 Đường ABC")
    private String note;       // Ghi chú (ví dụ: "Gọi trước khi giao")
    private boolean isDefault; // Địa chỉ mặc định

    // Constructor
    public Address() {}

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public Address(String addressId, String addressName, String address, String note, boolean isDefault) {
        this.addressId = addressId;
        this.addressName = addressName;
        this.address = address;
        this.note = note;
        this.isDefault = isDefault;
    }

    // Getters và Setters
    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
