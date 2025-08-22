package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class AccountSetupRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private String taxRegistrationNumber;
    private String customerId;
    private String creditCard;
    private String customerName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private User.UserType userType;
    private Integer storeId;
    
    public AccountSetupRequest() {}
    
    public AccountSetupRequest(String username, String password, String taxRegistrationNumber, 
                              String customerId, String creditCard, String customerName,
                              String firstName, String lastName, String email, String phone, String address) {
        this.username = username;
        this.password = password;
        this.taxRegistrationNumber = taxRegistrationNumber;
        this.customerId = customerId;
        this.creditCard = creditCard;
        this.customerName = customerName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getTaxRegistrationNumber() { return taxRegistrationNumber; }
    public void setTaxRegistrationNumber(String taxRegistrationNumber) { this.taxRegistrationNumber = taxRegistrationNumber; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCreditCard() { return creditCard; }
    public void setCreditCard(String creditCard) { this.creditCard = creditCard; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public User.UserType getUserType() { return userType; }
    public void setUserType(User.UserType userType) { this.userType = userType; }
    
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    
    // Helper methods
    public boolean isBrandUser() {
        return userType == User.UserType.BRAND_USER;
    }
    
    public boolean isStoreSpecific() {
        return userType == User.UserType.STORE_SPECIFIC;
    }
    
    // Validation method
    public boolean isValid() {
        // Basic fields required for all users
        boolean basicValid = username != null && !username.trim().isEmpty() &&
                           password != null && !password.trim().isEmpty() &&
                           firstName != null && !firstName.trim().isEmpty() &&
                           lastName != null && !lastName.trim().isEmpty() &&
                           email != null && !email.trim().isEmpty() &&
                           phone != null && !phone.trim().isEmpty() &&
                           address != null && !address.trim().isEmpty() &&
                           userType != null;
        
        if (!basicValid) {
            return false;
        }
        
        // Additional validation based on user type
        if (isBrandUser()) {
            // Brand users must provide subscription details
            return taxRegistrationNumber != null && !taxRegistrationNumber.trim().isEmpty() &&
                   customerId != null && !customerId.trim().isEmpty() &&
                   creditCard != null && !creditCard.trim().isEmpty() &&
                   customerName != null && !customerName.trim().isEmpty();
        } else if (isStoreSpecific()) {
            // Store-specific users must select a store
            return storeId != null;
        }
        
        return true;
    }
}
