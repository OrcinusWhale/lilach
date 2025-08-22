package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class EmployeeManagementResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private Employee employee; // For single employee operations
    private List<Employee> employees; // For get all employees
    
    public EmployeeManagementResponse() {}
    
    public EmployeeManagementResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public EmployeeManagementResponse(boolean success, String message, Employee employee) {
        this.success = success;
        this.message = message;
        this.employee = employee;
    }
    
    public EmployeeManagementResponse(boolean success, String message, List<Employee> employees) {
        this.success = success;
        this.message = message;
        this.employees = employees;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
}
