package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class OrderCancellationRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private int userId;
    private String cancellationReason;
    
    // Default constructor
    public OrderCancellationRequest() {
    }
    
    // Constructor
    public OrderCancellationRequest(Long orderId, int userId, String cancellationReason) {
        this.orderId = orderId;
        this.userId = userId;
        this.cancellationReason = cancellationReason;
    }
    
    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    @Override
    public String toString() {
        return "OrderCancellationRequest{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", cancellationReason='" + cancellationReason + '\'' +
                '}';
    }
}
