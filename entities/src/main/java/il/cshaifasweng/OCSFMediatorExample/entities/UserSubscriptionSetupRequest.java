package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class UserSubscriptionSetupRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int userId;
    private String taxRegistrationNumber;
    private String customerId;
    private String creditCard;
    private String customerName;
    
    public UserSubscriptionSetupRequest() {}
    
    public UserSubscriptionSetupRequest(int userId, String taxRegistrationNumber, 
                                       String customerId, String creditCard, String customerName) {
        this.userId = userId;
        this.taxRegistrationNumber = taxRegistrationNumber;
        this.customerId = customerId;
        this.creditCard = creditCard;
        this.customerName = customerName;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getTaxRegistrationNumber() { return taxRegistrationNumber; }
    public void setTaxRegistrationNumber(String taxRegistrationNumber) { this.taxRegistrationNumber = taxRegistrationNumber; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCreditCard() { return creditCard; }
    public void setCreditCard(String creditCard) { this.creditCard = creditCard; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    // Validation method
    public boolean isValid() {
        return taxRegistrationNumber != null && !taxRegistrationNumber.trim().isEmpty() &&
               customerId != null && !customerId.trim().isEmpty() &&
               creditCard != null && !creditCard.trim().isEmpty() &&
               customerName != null && !customerName.trim().isEmpty();
    }
}
