package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.User;

public class UserSession {
    private static User currentUser;
    private static String sessionId;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void setCurrentUser(User user, String sessionId) {
        currentUser = user;
        UserSession.sessionId = sessionId;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getSessionId() {
        return sessionId;
    }

    public static boolean isLoggedIn() {
        return currentUser != null && sessionId != null;
    }

    public static void clearSession() {
        currentUser = null;
        sessionId = null;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public static boolean hasValidSession() {
        return currentUser != null && sessionId != null && !sessionId.isEmpty();
    }
}
