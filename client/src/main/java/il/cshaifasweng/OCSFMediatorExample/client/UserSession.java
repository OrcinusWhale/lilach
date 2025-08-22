package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.User;

public class UserSession {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
}
