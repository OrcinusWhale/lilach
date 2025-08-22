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
    private SubscriptionType subscriptionType = SubscriptionType.NONE;
    
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private boolean isSubscriptionActive = false;
    
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

    public boolean isSubscriptionActive() { return isSubscriptionActive; }
    public void setSubscriptionActive(boolean subscriptionActive) { isSubscriptionActive = subscriptionActive; }

    public List<String> getSubscriptionHistory() { return subscriptionHistory; }
    public void setSubscriptionHistory(List<String> subscriptionHistory) { this.subscriptionHistory = subscriptionHistory; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean canRequestSubscription() {
        // Business logic: users can request subscription if they don't have an active one
        return !isSubscriptionActive;
    }

    public void addSubscriptionHistoryEntry(String entry) {
        subscriptionHistory.add(entry);
    }

    public enum UserType {
        CUSTOMER, ADMIN, MANAGER
    }

    public enum SubscriptionType {
        NONE, BASIC, PREMIUM, VIP
    }
}
