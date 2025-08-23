package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class OrderDetailsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private int userId;

    public OrderDetailsRequest() {}

    public OrderDetailsRequest(Long orderId, int userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
