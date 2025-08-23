package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;

/**
 * Manages user sessions to ensure only one active session per user
 */
public class SessionManager {
    
    // Map of userId -> SessionInfo
    private static final ConcurrentMap<Integer, SessionInfo> activeUserSessions = new ConcurrentHashMap<>();
    
    // Map of sessionId -> userId for quick lookup during cleanup
    private static final ConcurrentMap<String, Integer> sessionToUser = new ConcurrentHashMap<>();
    
    // Map of ConnectionToClient -> sessionId for connection-based cleanup
    private static final ConcurrentMap<ConnectionToClient, String> connectionSessions = new ConcurrentHashMap<>();

    /**
     * Represents an active user session
     */
    public static class SessionInfo {
        private final String sessionId;
        private final int userId;
        private final ConnectionToClient connection;
        private final long loginTime;
        
        public SessionInfo(String sessionId, int userId, ConnectionToClient connection) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.connection = connection;
            this.loginTime = System.currentTimeMillis();
        }
        
        public String getSessionId() { return sessionId; }
        public int getUserId() { return userId; }
        public ConnectionToClient getConnection() { return connection; }
        public long getLoginTime() { return loginTime; }
    }

    /**
     * Attempts to create a new session for a user
     * @param userId The user ID
     * @param connection The client connection
     * @return SessionInfo if successful, null if user already has an active session
     */
    public static synchronized SessionInfo createSession(int userId, ConnectionToClient connection) {
        // Check if user already has an active session
        if (activeUserSessions.containsKey(userId)) {
            System.out.println("Session creation denied: User " + userId + " already has an active session");
            return null;
        }
        
        // Generate unique session ID
        String sessionId = generateSessionId();
        
        // Create session
        SessionInfo sessionInfo = new SessionInfo(sessionId, userId, connection);
        
        // Store session mappings
        activeUserSessions.put(userId, sessionInfo);
        sessionToUser.put(sessionId, userId);
        connectionSessions.put(connection, sessionId);
        
        System.out.println("Session created successfully: " + sessionId + " for user " + userId);
        return sessionInfo;
    }

    /**
     * Validates if a session is still active
     * @param sessionId The session ID to validate
     * @param userId The user ID associated with the session
     * @return true if session is valid and active
     */
    public static boolean isSessionValid(String sessionId, int userId) {
        SessionInfo session = activeUserSessions.get(userId);
        return session != null && session.getSessionId().equals(sessionId);
    }

    /**
     * Checks if a user has an active session
     * @param userId The user ID to check
     * @return true if user has an active session
     */
    public static boolean hasActiveSession(int userId) {
        return activeUserSessions.containsKey(userId);
    }

    /**
     * Terminates a user's session
     * @param userId The user ID whose session should be terminated
     * @return true if session was terminated, false if no session existed
     */
    public static synchronized boolean terminateSession(int userId) {
        SessionInfo session = activeUserSessions.remove(userId);
        if (session != null) {
            sessionToUser.remove(session.getSessionId());
            connectionSessions.remove(session.getConnection());
            System.out.println("Session terminated: " + session.getSessionId() + " for user " + userId);
            return true;
        }
        return false;
    }

    /**
     * Terminates a session by session ID
     * @param sessionId The session ID to terminate
     * @return true if session was terminated, false if session didn't exist
     */
    public static synchronized boolean terminateSessionById(String sessionId) {
        Integer userId = sessionToUser.get(sessionId);
        if (userId != null) {
            return terminateSession(userId);
        }
        return false;
    }

    /**
     * Cleans up sessions when a client connection is lost
     * @param connection The disconnected client connection
     * @return true if a session was cleaned up, false if no session was associated
     */
    public static synchronized boolean cleanupConnectionSession(ConnectionToClient connection) {
        String sessionId = connectionSessions.remove(connection);
        if (sessionId != null) {
            Integer userId = sessionToUser.remove(sessionId);
            if (userId != null) {
                activeUserSessions.remove(userId);
                System.out.println("Session cleaned up due to client disconnect: " + sessionId + " for user " + userId);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets session information for a user
     * @param userId The user ID
     * @return SessionInfo if user has an active session, null otherwise
     */
    public static SessionInfo getSessionInfo(int userId) {
        return activeUserSessions.get(userId);
    }

    /**
     * Gets the total number of active sessions
     * @return Number of active sessions
     */
    public static int getActiveSessionCount() {
        return activeUserSessions.size();
    }

    /**
     * Generates a unique session ID
     * @return A unique session ID string
     */
    private static String generateSessionId() {
        return "SESSION_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * Terminates all sessions (useful for server shutdown)
     */
    public static synchronized void terminateAllSessions() {
        System.out.println("Terminating all active sessions (" + activeUserSessions.size() + " sessions)");
        activeUserSessions.clear();
        sessionToUser.clear();
        connectionSessions.clear();
    }
}
