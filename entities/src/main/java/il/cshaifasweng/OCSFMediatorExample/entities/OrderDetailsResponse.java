package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class OrderDetailsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Order order;

    public OrderDetailsResponse() {}

    public OrderDetailsResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public OrderDetailsResponse(boolean success, String message, Order order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
