package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private String address;
    
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.CUSTOMER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType = SubscriptionType.NONE;
    
    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;
    
    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    
    @Column(name = "is_subscription_active")
    private boolean isSubscriptionActive = false;
    
    // Account setup fields for subscription
    @Column(name = "tax_registration_number")
    private String taxRegistrationNumber;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "credit_card") // Should be encrypted in production
    private String creditCard;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "account_value")
    private double accountValue = 0.0; // Annual subscription value (100₪)
    
    @ElementCollection
    @CollectionTable(name = "user_subscription_history", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> subscriptionHistory = new ArrayList<>();

    public User() {}

    public User(String username, String password, String firstName, String lastName, 
                String email, String phone, String address) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public SubscriptionType getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(SubscriptionType subscriptionType) { this.subscriptionType = subscriptionType; }

    public LocalDateTime getSubscriptionStartDate() { return subscriptionStartDate; }
    public void setSubscriptionStartDate(LocalDateTime subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }

    public LocalDateTime getSubscriptionEndDate() { return subscriptionEndDate; }
    public void setSubscriptionEndDate(LocalDateTime subscriptionEndDate) { this.subscriptionEndDate = subscriptionEndDate; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public boolean isSubscriptionActive() { return isSubscriptionActive; }
    public void setSubscriptionActive(boolean subscriptionActive) { isSubscriptionActive = subscriptionActive; }

    public List<String> getSubscriptionHistory() { return subscriptionHistory; }
    public void setSubscriptionHistory(List<String> subscriptionHistory) { this.subscriptionHistory = subscriptionHistory; }

    public String getTaxRegistrationNumber() { return taxRegistrationNumber; }
    public void setTaxRegistrationNumber(String taxRegistrationNumber) { this.taxRegistrationNumber = taxRegistrationNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCreditCard() { return creditCard; }
    public void setCreditCard(String creditCard) { this.creditCard = creditCard; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getAccountValue() { return accountValue; }
    public void setAccountValue(double accountValue) { this.accountValue = accountValue; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean canRequestSubscription() {
        // Only brand users can request subscriptions
        return isBrandUser() && !isSubscriptionActive;
    }

    public void addSubscriptionHistoryEntry(String entry) {
        subscriptionHistory.add(entry);
    }

    // Authorization methods
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isEmployee() {
        return userType == UserType.EMPLOYEE;
    }

    public boolean isCustomer() {
        return userType == UserType.CUSTOMER;
    }

    public boolean canEditEmployeeDetails() {
        return isAdmin();
    }

    public boolean canAccessAdminPanel() {
        return isAdmin();
    }

    // Subscription-related business logic
    public boolean hasValidAccountSetup() {
        return taxRegistrationNumber != null && !taxRegistrationNumber.trim().isEmpty() &&
               customerId != null && !customerId.trim().isEmpty() &&
               creditCard != null && !creditCard.trim().isEmpty() &&
               customerName != null && !customerName.trim().isEmpty();
    }

    public boolean canPlaceOrders() {
        if (isStoreSpecific()) {
            return true; // Store-specific users can always place orders
        }
        return isBrandUser() && hasValidAccountSetup() && isSubscriptionActive;
    }

    public double calculateDiscount(double purchaseAmount) {
        if (isSubscriptionActive && purchaseAmount > 50.0) {
            return purchaseAmount * 0.10; // 10% discount
        }
        return 0.0;
    }

    public double calculateFinalPrice(double originalPrice) {
        return originalPrice - calculateDiscount(originalPrice);
    }

    public void setupAnnualSubscription(String taxRegNum, String custId, String creditCardNum, String custName) {
        if (!isBrandUser()) {
            throw new IllegalStateException("Only brand users can setup annual subscriptions");
        }
        this.taxRegistrationNumber = taxRegNum;
        this.customerId = custId;
        this.creditCard = creditCardNum;
        this.customerName = custName;
        this.accountValue = 100.0; // Annual subscription value
        this.subscriptionType = SubscriptionType.BASIC;
        this.subscriptionStartDate = LocalDateTime.now();
        this.subscriptionEndDate = LocalDateTime.now().plusYears(1);
        this.isSubscriptionActive = true;
        addSubscriptionHistoryEntry("Annual subscription activated on " + LocalDateTime.now());
    }

    // Business logic methods for store-specific and brand users
    public boolean isStoreSpecific() {
        return userType == UserType.STORE_SPECIFIC;
    }
    
    public boolean isBrandUser() {
        return userType == UserType.BRAND_USER;
    }

    public enum UserType {
        CUSTOMER, EMPLOYEE, ADMIN, STORE_SPECIFIC, BRAND_USER
    }

    public enum SubscriptionType {
        NONE, BASIC, PREMIUM, VIP
    }
}
