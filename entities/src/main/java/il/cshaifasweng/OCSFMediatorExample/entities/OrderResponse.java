package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class OrderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Order order;
    private Long orderId;

    public OrderResponse() {}

    public OrderResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public OrderResponse(boolean success, String message, Order order) {
        this(success, message);
        this.order = order;
        if (order != null) {
            this.orderId = order.getOrderId();
        }
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { 
        this.order = order;
        if (order != null) {
            this.orderId = order.getOrderId();
        }
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
