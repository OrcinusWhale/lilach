package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class EmployeeManagementRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private RequestType requestType;
    private User adminUser; // User making the request
    private Employee employeeData; // Employee data for create/update
    private int employeeId; // For get/delete operations
    
    public EmployeeManagementRequest() {}
    
    public EmployeeManagementRequest(RequestType requestType, User adminUser) {
        this.requestType = requestType;
        this.adminUser = adminUser;
    }
    
    public EmployeeManagementRequest(RequestType requestType, User adminUser, Employee employeeData) {
        this.requestType = requestType;
        this.adminUser = adminUser;
        this.employeeData = employeeData;
    }
    
    public EmployeeManagementRequest(RequestType requestType, User adminUser, int employeeId) {
        this.requestType = requestType;
        this.adminUser = adminUser;
        this.employeeId = employeeId;
    }
    
    // Getters and Setters
    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }
    
    public User getAdminUser() { return adminUser; }
    public void setAdminUser(User adminUser) { this.adminUser = adminUser; }
    
    public Employee getEmployeeData() { return employeeData; }
    public void setEmployeeData(Employee employeeData) { this.employeeData = employeeData; }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    
    public enum RequestType {
        GET_ALL_EMPLOYEES,
        GET_EMPLOYEE_BY_ID,
        CREATE_EMPLOYEE,
        UPDATE_EMPLOYEE,
        DELETE_EMPLOYEE
    }
}
