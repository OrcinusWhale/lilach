package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

/**
 * Request DTO for assigning a complaint to an employee.
 */
public class ComplaintAssignRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int complaintId;
    private String employeeUsername;

    // Default constructor
    public ComplaintAssignRequest() {}

    // All-args constructor
    public ComplaintAssignRequest(int complaintId, String employeeUsername) {
        this.complaintId = complaintId;
        this.employeeUsername = employeeUsername;
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

    @Override
    public String toString() {
        return "ComplaintAssignRequest{" +
                "complaintId=" + complaintId +
                ", employeeUsername='" + employeeUsername + '\'' +
                '}';
    }
}
