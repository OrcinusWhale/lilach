package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SessionResponse implements Serializable {
    private boolean success;
    private String message;
    private String sessionId;
    private boolean activeSessionExists;

    public SessionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public SessionResponse(boolean success, String message, String sessionId) {
        this.success = success;
        this.message = message;
        this.sessionId = sessionId;
    }

    public SessionResponse(boolean success, String message, boolean activeSessionExists) {
        this.success = success;
        this.message = message;
        this.activeSessionExists = activeSessionExists;
    }

    public SessionResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isActiveSessionExists() {
        return activeSessionExists;
    }

    public void setActiveSessionExists(boolean activeSessionExists) {
        this.activeSessionExists = activeSessionExists;
    }
}
