package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class ComplaintResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String etaIso;

    // Default constructor
    public ComplaintResponse() {}

    // All-args constructor
    public ComplaintResponse(boolean success, String message, String etaIso) {
        this.success = success;
        this.message = message;
        this.etaIso = etaIso;
    }


    // Getters and Setters
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

    public String getEtaIso() {
        return etaIso;
    }

    public void setEtaIso(String etaIso) {
        this.etaIso = etaIso;
    }

}
