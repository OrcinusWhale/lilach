package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AuthenticationService {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Authenticate user with username and password with session management
     */
    public static LoginResponse authenticateUser(LoginRequest loginRequest, ConnectionToClient connection) {
        try {
            System.out.println("Authenticating user: " + loginRequest.getUsername());
            
            // Find user in database
            CriteriaBuilder builder = App.session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.where(builder.equal(root.get("username"), loginRequest.getUsername()));
            
            List<User> users = App.session.createQuery(query).getResultList();
            
            if (users.isEmpty()) {
                System.out.println("User not found: " + loginRequest.getUsername());
                return new LoginResponse(false, "Invalid username or password", null);
            }
            
            User user = users.get(0);
            System.out.println("Found user: " + user.getUsername() + ", stored password length: " + user.getPassword().length());
            
            // Verify password
            if (verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                // Check if user already has an active session
                if (SessionManager.hasActiveSession(user.getUserId())) {
                    System.out.println("Login denied: User " + user.getUsername() + " already has an active session");
                    return new LoginResponse(false, "SESSION_CONFLICT: User already logged in from another location. Please logout from other session first.");
                }
                
                // Create new session
                SessionManager.SessionInfo sessionInfo = SessionManager.createSession(user.getUserId(), connection);
                if (sessionInfo == null) {
                    return new LoginResponse(false, "Failed to create session. Please try again.");
                }
                
                // Don't send password in response
                User safeUser = createSafeUserCopy(user);
                
                System.out.println("Login successful for: " + user.getUsername() + " (Role: " + user.getUserType() + 
                                 ") with session: " + sessionInfo.getSessionId());
                
                return new LoginResponse(true, "Login successful", safeUser);
            } else {
                System.out.println("Password verification failed for user: " + user.getUsername());
                return new LoginResponse(false, "Invalid username or password", null);
            }
            
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return new LoginResponse(false, "Authentication failed: Server error", null);
        }
    }
    
    /**
     * Backward compatibility method for authentication without connection
     */
    public static LoginResponse authenticateUser(LoginRequest loginRequest) {
        return new LoginResponse(false, "Connection required for session management");
    }
    
    /**
     * Register a new user (for customers)
     */
    public static LoginResponse registerUser(User newUser) {
        try {
            // Check if user already exists
            if (userExists(newUser.getUsername())) {
                return new LoginResponse(false, "Username already exists", null);
            }
            
            // Hash password before saving
            String hashedPassword = hashPassword(newUser.getPassword());
            newUser.setPassword(hashedPassword);
            
            // Set default role as customer
            if (newUser.getUserType() == null) {
                newUser.setUserType(User.UserType.CUSTOMER);
            }
            
            // Save new user
            App.session.beginTransaction();
            App.session.save(newUser);
            App.session.flush();
            App.session.getTransaction().commit();
            
            System.out.println("User registered successfully: " + newUser.getUsername());
            return new LoginResponse(true, "Registration successful", null);
            
        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
            return new LoginResponse(false, "Registration failed: Server error", null);
        }
    }
    
    /**
     * Register a new employee (for admin use)
     */
    public static boolean registerEmployee(User employeeUser, Employee employeeData) {
        try {
            App.session.beginTransaction();
            
            // Hash password
            String hashedPassword = hashPassword(employeeUser.getPassword());
            employeeUser.setPassword(hashedPassword);
            employeeUser.setUserType(User.UserType.EMPLOYEE);
            
            // Save user first
            App.session.save(employeeUser);
            App.session.flush();
            
            // Set user reference in employee data
            employeeData.setUser(employeeUser);
            
            // Save employee data
            App.session.save(employeeData);
            App.session.flush();
            
            App.session.getTransaction().commit();
            
            System.out.println("Employee registered successfully: " + employeeUser.getUsername());
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during employee registration: " + e.getMessage());
            e.printStackTrace();
            if (App.session.getTransaction().isActive()) {
                App.session.getTransaction().rollback();
            }
            return false;
        }
    }
    
    /**
     * Check if user has required role
     */
    public static boolean hasRole(User user, User.UserType requiredRole) {
        return user != null && user.getUserType() == requiredRole;
    }
    
    /**
     * Check if user can edit employee details
     */
    public static boolean canEditEmployeeDetails(User user) {
        return user != null && user.canEditEmployeeDetails();
    }
    
    /**
     * Hash password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Combine salt and hash
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            System.out.println("Verifying password. Stored hash length: " + storedHash.length() + ", contains '=': " + storedHash.contains("="));
            System.out.println("First 20 chars of stored hash: " + storedHash.substring(0, Math.min(20, storedHash.length())));
            
            // Check if it's likely a Base64 encoded hash (length suggests it's hashed)
            if (storedHash.length() > 40) { // Our hashed passwords should be longer than 40 chars
                System.out.println("Attempting to verify as hashed password");
                try {
                    byte[] combined = Base64.getDecoder().decode(storedHash);
                    
                    // Extract salt
                    byte[] salt = new byte[SALT_LENGTH];
                    System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
                    
                    // Extract hash
                    byte[] storedPasswordHash = new byte[combined.length - SALT_LENGTH];
                    System.arraycopy(combined, SALT_LENGTH, storedPasswordHash, 0, storedPasswordHash.length);
                    
                    // Hash provided password with extracted salt
                    MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
                    md.update(salt);
                    byte[] providedPasswordHash = md.digest(password.getBytes());
                    
                    // Compare hashes
                    boolean matches = MessageDigest.isEqual(storedPasswordHash, providedPasswordHash);
                    System.out.println("Hashed password verification result: " + matches);
                    return matches;
                } catch (Exception e) {
                    System.err.println("Failed to decode as Base64 hash: " + e.getMessage());
                    // Fall through to plain text comparison
                }
            }
            
            // For backward compatibility with plain text passwords
            System.out.println("Using plain text password comparison for backward compatibility");
            boolean matches = password.equals(storedHash);
            System.out.println("Plain text password verification result: " + matches);
            return matches;
            
        } catch (Exception e) {
            System.err.println("Password verification failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if username already exists
     */
    private static boolean userExists(String username) {
        try {
            CriteriaBuilder builder = App.session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<User> root = query.from(User.class);
            query.select(builder.count(root));
            query.where(builder.equal(root.get("username"), username));
            
            Long count = App.session.createQuery(query).getSingleResult();
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create a safe copy of user without sensitive data
     */
    private static User createSafeUserCopy(User user) {
        User safeUser = new User();
        safeUser.setUserId(user.getUserId());
        safeUser.setUsername(user.getUsername());
        safeUser.setFirstName(user.getFirstName());
        safeUser.setLastName(user.getLastName());
        safeUser.setEmail(user.getEmail());
        safeUser.setPhone(user.getPhone());
        safeUser.setAddress(user.getAddress());
        safeUser.setUserType(user.getUserType());
        safeUser.setSubscriptionType(user.getSubscriptionType());
        safeUser.setSubscriptionStartDate(user.getSubscriptionStartDate());
        safeUser.setSubscriptionEndDate(user.getSubscriptionEndDate());
        safeUser.setSubscriptionActive(user.isSubscriptionActive());
        safeUser.setSubscriptionHistory(user.getSubscriptionHistory());
        
        // Include subscription account fields (but not sensitive credit card info)
        safeUser.setTaxRegistrationNumber(user.getTaxRegistrationNumber());
        safeUser.setCustomerId(user.getCustomerId());
        safeUser.setCustomerName(user.getCustomerName());
        safeUser.setAccountValue(user.getAccountValue());
        
        // Don't copy password or credit card number for security
        return safeUser;
    }
    
    /**
     * Logout user by terminating their session
     */
    public static boolean logoutUser(int userId) {
        return SessionManager.terminateSession(userId);
    }
    
    /**
     * Validate if a session is still active and belongs to the user
     */
    public static boolean validateSession(String sessionId, int userId) {
        return SessionManager.isSessionValid(sessionId, userId);
    }
    
    /**
     * Get all active sessions count (delegates to SessionManager)
     */
    public static int getActiveSessionsCount() {
        return SessionManager.getActiveSessionCount();
    }
    
    /**
     * Clean up session when client disconnects
     */
    public static boolean cleanupSession(ConnectionToClient connection) {
        return SessionManager.cleanupConnectionSession(connection);
    }
}
