package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class OrderHistoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Order> orders;
    private boolean success;
    private String message;
    
    public OrderHistoryResponse() {}
    
    public OrderHistoryResponse(List<Order> orders, boolean success, String message) {
        this.orders = orders;
        this.success = success;
        this.message = message;
    }
    
    public OrderHistoryResponse(List<Order> orders) {
        this.orders = orders;
        this.success = true;
        this.message = "Orders retrieved successfully";
    }
    
    public OrderHistoryResponse(String errorMessage) {
        this.success = false;
        this.message = errorMessage;
    }
    
    public List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "OrderHistoryResponse{" +
                "orders=" + (orders != null ? orders.size() : 0) + " orders" +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
