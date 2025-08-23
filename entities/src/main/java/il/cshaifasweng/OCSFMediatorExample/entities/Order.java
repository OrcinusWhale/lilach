package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "requested_delivery_date", nullable = false)
    private LocalDateTime requestedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType; // DELIVERY or PICKUP

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "delivery_fee")
    private double deliveryFee = 0.0;

    @Column(name = "greeting_card_message", length = 500)
    private String greetingCardMessage;

    @Column(name = "has_greeting_card")
    private boolean hasGreetingCard = false;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "discount_amount")
    private double discountAmount = 0.0;

    @Column(name = "final_amount", nullable = false)
    private double finalAmount;

    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;

    @Column(name = "created_via", nullable = false)
    private String createdVia; // "WEB" or "IN_STORE"

    // Default constructor
    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    // Constructor
    public Order(User user, Store store, OrderType orderType, LocalDateTime requestedDeliveryDate, String createdVia) {
        this();
        this.user = user;
        this.store = store;
        this.orderType = orderType;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.createdVia = createdVia;
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDateTime getRequestedDeliveryDate() { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(LocalDateTime requestedDeliveryDate) { this.requestedDeliveryDate = requestedDeliveryDate; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getGreetingCardMessage() { return greetingCardMessage; }
    public void setGreetingCardMessage(String greetingCardMessage) { 
        this.greetingCardMessage = greetingCardMessage;
        this.hasGreetingCard = (greetingCardMessage != null && !greetingCardMessage.trim().isEmpty());
    }

    public boolean isHasGreetingCard() { return hasGreetingCard; }
    public void setHasGreetingCard(boolean hasGreetingCard) { this.hasGreetingCard = hasGreetingCard; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getCreatedVia() { return createdVia; }
    public void setCreatedVia(String createdVia) { this.createdVia = createdVia; }

    // Helper methods
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotals();
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        calculateTotals();
    }

    public void calculateTotals() {
        this.totalAmount = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        // Apply user discount if applicable
        if (user != null) {
            this.discountAmount = user.calculateDiscount(this.totalAmount);
        }
        
        this.finalAmount = this.totalAmount - this.discountAmount + this.deliveryFee;
    }

    public boolean isDelivery() {
        return orderType == OrderType.DELIVERY;
    }

    public boolean isPickup() {
        return orderType == OrderType.PICKUP;
    }

    public boolean canBeCancelled() {
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED;
    }

    public boolean isCompleted() {
        return orderStatus == OrderStatus.DELIVERED || orderStatus == OrderStatus.PICKED_UP;
    }

    // Enums
    public enum OrderType {
        DELIVERY, PICKUP
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, PICKED_UP, CANCELLED
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + user.getFullName() + " - " + orderStatus;
    }
}
