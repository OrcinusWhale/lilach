package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SessionRequest implements Serializable {
    private String requestType;
    private int userId;
    private String sessionId;

    public SessionRequest(String requestType, int userId, String sessionId) {
        this.requestType = requestType;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public SessionRequest() {}

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public static class RequestType {
        public static final String LOGOUT = "LOGOUT";
        public static final String VALIDATE_SESSION = "VALIDATE_SESSION";
    }
}
