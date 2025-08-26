package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request DTO for making a compensation decision on a complaint.
 */
public class ComplaintDecisionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int complaintId;
    private String employeeUsername;
    private BigDecimal compensationAmount;
    private String currency;
    private String note;

    // Default constructor
    public ComplaintDecisionRequest() {}

    // All-args constructor
    public ComplaintDecisionRequest(int complaintId, String employeeUsername, 
                                  BigDecimal compensationAmount, String currency, String note) {
        this.complaintId = complaintId;
        this.employeeUsername = employeeUsername;
        this.compensationAmount = compensationAmount;
        this.currency = currency;
        this.note = note;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public void setEmployeeUsername(String employeeUsername) {
        this.employeeUsername = employeeUsername;
    }

    public BigDecimal getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(BigDecimal compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "ComplaintDecisionRequest{" +
                "complaintId=" + complaintId +
                ", employeeUsername='" + employeeUsername + '\'' +
                ", compensationAmount=" + compensationAmount +
                ", currency='" + currency + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
