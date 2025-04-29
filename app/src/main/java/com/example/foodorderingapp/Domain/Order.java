package com.example.foodorderingapp.Domain;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Order {
    // Thuộc tính
    private String orderId;          // ID đơn hàng (tự động sinh bởi Firestore)
    private Timestamp timestamp;     // Thời điểm tạo đơn hàng (Firestore Timestamp)
    private double totalPrice;       // Tổng tiền
    private OrderStatus status;      // Trạng thái đơn hàng (enum)
    private Map<String, Integer> items = new HashMap<>(); // Danh sách món ăn (foodId -> quantity)
    private Address deliveryAddress; // Địa chỉ giao hàng

    // Enum định nghĩa các trạng thái hợp lệ
    public enum OrderStatus {
        PENDING("Đang xử lý"),
        PROCESSING("Đang chuẩn bị"),
        SHIPPED("Đang giao"),
        DELIVERED("Đã giao"),
        CANCELLED("Đã hủy");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructor mặc định (bắt buộc cho Firestore)
    public Order() {}

    // Constructor chính
    public Order(double totalPrice, OrderStatus status, Map<String, Integer> items, Address deliveryAddress) {
        this.timestamp = Timestamp.now(); // Tự động gán thời gian hiện tại
        this.totalPrice = totalPrice;
        this.status = status;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
    }

    // Getter/Setter
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Map<String, Integer> getItems() { return items; }
    public void setItems(Map<String, Integer> items) { this.items = items; }

    public Address getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(Address deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    // Phương thức tiện ích
    public void addItem(String foodId, int quantity) {
        items.put(foodId, items.getOrDefault(foodId, 0) + quantity);
    }

    public void removeItem(String foodId) {
        items.remove(foodId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", timestamp=" + timestamp.toDate() +
                ", totalPrice=" + totalPrice +
                ", status=" + status.getDisplayName() +
                ", items=" + items +
                ", deliveryAddress=" + deliveryAddress +
                '}';
    }
}
