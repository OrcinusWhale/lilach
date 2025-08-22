package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SubscriptionResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private User.SubscriptionType approvedSubscriptionType;
    
    public SubscriptionResponse() {}
    
    public SubscriptionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public SubscriptionResponse(boolean success, String message, User.SubscriptionType approvedSubscriptionType) {
        this.success = success;
        this.message = message;
        this.approvedSubscriptionType = approvedSubscriptionType;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public User.SubscriptionType getApprovedSubscriptionType() { return approvedSubscriptionType; }
    public void setApprovedSubscriptionType(User.SubscriptionType approvedSubscriptionType) { 
        this.approvedSubscriptionType = approvedSubscriptionType; 
    }
}
