package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

/**
 * Response DTO for complaint assignment and decision actions.
 */
public class ComplaintActionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;

    // Default constructor
    public ComplaintActionResponse() {}

    // All-args constructor
    public ComplaintActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

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

    @Override
    public String toString() {
        return "ComplaintActionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
