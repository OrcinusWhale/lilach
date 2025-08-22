package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class AccountSetupResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private User user;
    private double accountValue;
    
    public AccountSetupResponse() {}
    
    public AccountSetupResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AccountSetupResponse(boolean success, String message, User user, double accountValue) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.accountValue = accountValue;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public double getAccountValue() { return accountValue; }
    public void setAccountValue(double accountValue) { this.accountValue = accountValue; }
}
