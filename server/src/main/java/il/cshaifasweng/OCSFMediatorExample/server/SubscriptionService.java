package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.entities.UserSubscriptionSetupRequest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionService {
    private static final double ANNUAL_SUBSCRIPTION_VALUE = 100.0; // 100₪
    private static final double DISCOUNT_THRESHOLD = 50.0; // 50₪
    private static final double DISCOUNT_RATE = 0.10; // 10%
    
    private SessionFactory sessionFactory;
    
    public SubscriptionService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Sets up a new customer account with annual subscription
     */
    public AccountSetupResponse setupAccount(AccountSetupRequest request) {
        if (!request.isValid()) {
            return new AccountSetupResponse(false, "Invalid account setup data. All fields are required.");
        }
        
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Check if username already exists
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", request.getUsername());
            List<User> existingUsers = query.getResultList();
            
            if (!existingUsers.isEmpty()) {
                return new AccountSetupResponse(false, "Username already exists. Please choose a different username.");
            }
            
            // Check if email already exists
            Query<User> emailQuery = session.createQuery("FROM User WHERE email = :email", User.class);
            emailQuery.setParameter("email", request.getEmail());
            List<User> existingEmails = emailQuery.getResultList();
            
            if (!existingEmails.isEmpty()) {
                return new AccountSetupResponse(false, "Email already registered. Please use a different email.");
            }
            
            // Create new user with subscription
            User newUser = new User(
                request.getUsername(),
                AuthenticationService.hashPassword(request.getPassword()), // Hash password using existing service
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress()
            );
            
            // Setup annual subscription
            newUser.setupAnnualSubscription(
                request.getTaxRegistrationNumber(),
                request.getCustomerId(),
                request.getCreditCard(),
                request.getCustomerName()
            );
            
            session.save(newUser);
            transaction.commit();
            
            return new AccountSetupResponse(true, 
                "Account successfully created with annual subscription (100₪). You are now authorized to place orders with 10% discount on purchases above 50₪.", 
                newUser, 
                ANNUAL_SUBSCRIPTION_VALUE);
                
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            return new AccountSetupResponse(false, "Error creating account: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    /**
     * Calculates discount for a purchase amount
     */
    public double calculateDiscount(User user, double purchaseAmount) {
        if (user == null || !user.isSubscriptionActive()) {
            return 0.0;
        }
        
        if (purchaseAmount > DISCOUNT_THRESHOLD) {
            return purchaseAmount * DISCOUNT_RATE;
        }
        
        return 0.0;
    }
    
    /**
     * Calculates final price after discount
     */
    public double calculateFinalPrice(User user, double originalPrice) {
        double discount = calculateDiscount(user, originalPrice);
        return originalPrice - discount;
    }
    
    /**
     * Validates if user can place orders
     */
    public boolean canUserPlaceOrders(User user) {
        return user != null && user.canPlaceOrders();
    }
    
    /**
     * Gets subscription status for a user
     */
    public SubscriptionResponse getSubscriptionStatus(int userId) {
        Session session = sessionFactory.openSession();
        
        try {
            User user = session.get(User.class, userId);
            if (user == null) {
                return new SubscriptionResponse(false, "User not found");
            }
            
            if (user.isSubscriptionActive()) {
                return new SubscriptionResponse(true, 
                    String.format("Active subscription until %s. Account value: %.2f₪", 
                        user.getSubscriptionEndDate(), user.getAccountValue()),
                    user.getSubscriptionType());
            } else {
                return new SubscriptionResponse(false, "No active subscription");
            }
            
        } catch (Exception e) {
            return new SubscriptionResponse(false, "Error checking subscription status: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    /**
     * Renews an existing subscription
     */
    public SubscriptionResponse renewSubscription(int userId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            User user = session.get(User.class, userId);
            if (user == null) {
                return new SubscriptionResponse(false, "User not found");
            }
            
            if (!user.hasValidAccountSetup()) {
                return new SubscriptionResponse(false, "Account setup incomplete. Please complete account setup first.");
            }
            
            // Renew subscription for another year
            user.setSubscriptionStartDate(LocalDateTime.now());
            user.setSubscriptionEndDate(LocalDateTime.now().plusYears(1));
            user.setSubscriptionActive(true);
            user.setAccountValue(ANNUAL_SUBSCRIPTION_VALUE);
            user.addSubscriptionHistoryEntry("Subscription renewed on " + LocalDateTime.now());
            
            session.update(user);
            transaction.commit();
            
            return new SubscriptionResponse(true, 
                "Subscription successfully renewed for one year (100₪)", 
                user.getSubscriptionType());
                
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            return new SubscriptionResponse(false, "Error renewing subscription: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    /**
     * Sets up subscription for an existing logged-in user
     */
    public AccountSetupResponse setupSubscriptionForExistingUser(UserSubscriptionSetupRequest request) {
        if (!request.isValid()) {
            return new AccountSetupResponse(false, "Invalid subscription setup data. All fields are required.");
        }
        
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Get existing user
            User user = session.get(User.class, request.getUserId());
            if (user == null) {
                return new AccountSetupResponse(false, "User not found.");
            }
            
            // Check if user already has an active subscription
            if (user.isSubscriptionActive()) {
                return new AccountSetupResponse(false, "User already has an active subscription.");
            }
            
            // Debug: Check user status before subscription setup
            System.out.println("Before setup - User subscription active: " + user.isSubscriptionActive());
            System.out.println("Before setup - User account value: " + user.getAccountValue());
            
            // Setup annual subscription for existing user
            user.setupAnnualSubscription(
                request.getTaxRegistrationNumber(),
                request.getCustomerId(),
                request.getCreditCard(),
                request.getCustomerName()
            );
            
            // Debug: Check user status after subscription setup
            System.out.println("After setup - User subscription active: " + user.isSubscriptionActive());
            System.out.println("After setup - User account value: " + user.getAccountValue());
            System.out.println("After setup - User subscription type: " + user.getSubscriptionType());
            
            // Force Hibernate to flush changes to database
            session.merge(user);
            session.flush();
            transaction.commit();
            
            System.out.println("Database transaction committed successfully");
            
            // Verify the data was actually saved by re-querying
            User verifyUser = session.get(User.class, request.getUserId());
            System.out.println("Verification - User subscription active after commit: " + verifyUser.isSubscriptionActive());
            System.out.println("Verification - User account value after commit: " + verifyUser.getAccountValue());
            
            return new AccountSetupResponse(true, 
                "Annual subscription activated successfully (100₪)! You now have access to 10% discount on purchases above 50₪.", 
                user, 
                ANNUAL_SUBSCRIPTION_VALUE);
                
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            return new AccountSetupResponse(false, "Error setting up subscription: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    /**
     * Gets discount information for display
     */
    public String getDiscountInfo(User user, double purchaseAmount) {
        if (user == null || !user.isSubscriptionActive()) {
            return "No active subscription - no discount available";
        }
        
        double discount = calculateDiscount(user, purchaseAmount);
        if (discount > 0) {
            return String.format("Subscription discount: %.2f₪ (10%% off purchases above 50₪)", discount);
        } else {
            return "Purchase amount below 50₪ - no discount applied";
        }
    }
}
