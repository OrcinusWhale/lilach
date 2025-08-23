package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

/**
 * Client-side service for managing user sessions
 */
public class SessionService {
    
    private static SessionService instance;
    
    private SessionService() {
        // Simplified constructor without EventBus
    }
    
    public static SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }
    
    /**
     * Logout the current user - sends logout message to server then clears local session
     */
    public void logout() {
        System.out.println("Logout requested by user");
        
        // Send logout message to server if we have a current user (don't check sessionId since we removed it from LoginResponse)
        if (UserSession.getCurrentUser() != null) {
            try {
                int userId = UserSession.getCurrentUser().getUserId();
                String logoutMessage = "LOGOUT_USER:" + userId;
                System.out.println("=== CLIENT LOGOUT DEBUG: User found, userId: " + userId + " ===");
                System.out.println("=== CLIENT LOGOUT DEBUG: Sending logout message to server: " + logoutMessage + " ===");
                App.getClient().sendToServer(logoutMessage);
                System.out.println("=== CLIENT LOGOUT DEBUG: Logout message sent successfully ===");
            } catch (IOException e) {
                System.err.println("=== CLIENT LOGOUT DEBUG: Failed to send logout message to server: " + e.getMessage() + " ===");
                e.printStackTrace();
            }
        } else {
            System.out.println("=== CLIENT LOGOUT DEBUG: No current user found, skipping server logout message ===");
        }
        
        // Clear local session immediately 
        UserSession.clearSession();
        
        // Navigate back to login screen
        try {
            App.setRoot("login");
            System.out.println("Logout successful - navigated to login screen");
        } catch (IOException e) {
            System.err.println("Failed to navigate to login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cleanup method to be called when application is closing
     */
    public void cleanup() {
        if (UserSession.hasValidSession()) {
            // Clear session on cleanup
            UserSession.clearSession();
        }
    }
}
