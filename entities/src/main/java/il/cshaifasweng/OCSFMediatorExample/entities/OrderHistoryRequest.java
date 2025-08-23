package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class OrderHistoryRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    
    public OrderHistoryRequest() {}
    
    public OrderHistoryRequest(int userId) {
        this.userId = userId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "OrderHistoryRequest{userId=" + userId + "}";
    }
}
