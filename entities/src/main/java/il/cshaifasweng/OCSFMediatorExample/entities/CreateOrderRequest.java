package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CreateOrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private int storeId;
    private String orderType; // "DELIVERY" or "PICKUP"
    private LocalDateTime requestedDeliveryDate;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private String greetingCardMessage;
    private String specialInstructions;
    private String createdVia; // "WEB" or "IN_STORE"
    private String orderPriority; // "IMMEDIATE" or "SCHEDULED"

    public CreateOrderRequest() {}

    public CreateOrderRequest(int userId, int storeId, String orderType, 
                            LocalDateTime requestedDeliveryDate, String createdVia) {
        this.userId = userId;
        this.storeId = storeId;
        this.orderType = orderType;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.createdVia = createdVia;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public LocalDateTime getRequestedDeliveryDate() { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(LocalDateTime requestedDeliveryDate) { 
        this.requestedDeliveryDate = requestedDeliveryDate; 
    }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getGreetingCardMessage() { return greetingCardMessage; }
    public void setGreetingCardMessage(String greetingCardMessage) { 
        this.greetingCardMessage = greetingCardMessage; 
    }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { 
        this.specialInstructions = specialInstructions; 
    }

    public String getCreatedVia() { return createdVia; }
    public void setCreatedVia(String createdVia) { this.createdVia = createdVia; }

    public String getOrderPriority() { return orderPriority; }
    public void setOrderPriority(String orderPriority) { this.orderPriority = orderPriority; }
}
