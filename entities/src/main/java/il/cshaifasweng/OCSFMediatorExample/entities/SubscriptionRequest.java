package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SubscriptionRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    private User.SubscriptionType requestedSubscriptionType;
    private String justification;
    
    public SubscriptionRequest() {}
    
    public SubscriptionRequest(int userId, User.SubscriptionType requestedSubscriptionType, String justification) {
        this.userId = userId;
        this.requestedSubscriptionType = requestedSubscriptionType;
        this.justification = justification;
    }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public User.SubscriptionType getRequestedSubscriptionType() { return requestedSubscriptionType; }
    public void setRequestedSubscriptionType(User.SubscriptionType requestedSubscriptionType) { 
        this.requestedSubscriptionType = requestedSubscriptionType; 
    }
    
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
