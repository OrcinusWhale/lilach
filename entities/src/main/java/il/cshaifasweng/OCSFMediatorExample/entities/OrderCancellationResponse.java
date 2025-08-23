package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class OrderCancellationResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private double refundAmount;
    private int refundPercentage;
    private Long orderId;
    
    // Default constructor
    public OrderCancellationResponse() {
    }
    
    // Constructor for failed cancellation
    public OrderCancellationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.refundAmount = 0.0;
        this.refundPercentage = 0;
    }
    
    // Constructor for successful cancellation
    public OrderCancellationResponse(boolean success, String message, Long orderId, double refundAmount, int refundPercentage) {
        this.success = success;
        this.message = message;
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.refundPercentage = refundPercentage;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
    
    public int getRefundPercentage() { return refundPercentage; }
    public void setRefundPercentage(int refundPercentage) { this.refundPercentage = refundPercentage; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    @Override
    public String toString() {
        return "OrderCancellationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", refundAmount=" + refundAmount +
                ", refundPercentage=" + refundPercentage +
                ", orderId=" + orderId +
                '}';
    }
}
