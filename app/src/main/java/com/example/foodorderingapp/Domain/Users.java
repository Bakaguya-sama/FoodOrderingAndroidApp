package com.example.foodorderingapp.Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Users {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private List<Address> addresses = new ArrayList<>(); // Danh sách địa chỉ
    private Map<String, Boolean> wishlist = new HashMap<>(); // Danh sách yêu thích (key: foodId)
    private Map<String, Order> orders = new HashMap<>(); // Danh sách đơn hàng (key: orderId)

    public Users() {
    }

    public Users(String userId, String fullName, String email, String phone, String dateOfBirth, String gender) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Map<String, Boolean> getWishlist() {
        return wishlist;
    }

    public void setWishlist(Map<String, Boolean> wishlist) {
        this.wishlist = wishlist;
    }

    public Map<String, Order> getOrders() {
        return orders;
    }

    public void setOrders(Map<String, Order> orders) {
        this.orders = orders;
    }

    // Phương thức thêm địa chỉ
    public void addAddress(Address address) {
        addresses.add(address);
    }

    // Phương thức thêm món ăn vào wishlist
    public void addToWishlist(String foodId) {
        wishlist.put(foodId, true);
    }
}
