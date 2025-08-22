package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Employees")
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int employeeId;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(unique = true, nullable = false)
    private String employeeNumber;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false)
    private String position;
    
    @Column(nullable = false)
    private LocalDateTime hireDate;
    
    private Double salary;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    private String managerId;
    
    // Operational records - preserved when editing employee details
    @ElementCollection
    @CollectionTable(name = "employee_loans", joinColumns = @JoinColumn(name = "employee_id"))
    private List<String> loanRecords = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "employee_bills", joinColumns = @JoinColumn(name = "employee_id"))
    private List<String> billRecords = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "employee_work_history", joinColumns = @JoinColumn(name = "employee_id"))
    private List<String> workHistory = new ArrayList<>();

    public Employee() {}

    public Employee(User user, String employeeNumber, String department, String position) {
        this.user = user;
        this.employeeNumber = employeeNumber;
        this.department = department;
        this.position = position;
        this.hireDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDateTime getHireDate() { return hireDate; }
    public void setHireDate(LocalDateTime hireDate) { this.hireDate = hireDate; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public EmployeeStatus getStatus() { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public List<String> getLoanRecords() { return loanRecords; }
    public void setLoanRecords(List<String> loanRecords) { this.loanRecords = loanRecords; }

    public List<String> getBillRecords() { return billRecords; }
    public void setBillRecords(List<String> billRecords) { this.billRecords = billRecords; }

    public List<String> getWorkHistory() { return workHistory; }
    public void setWorkHistory(List<String> workHistory) { this.workHistory = workHistory; }

    // Business methods
    public void addLoanRecord(String loanRecord) {
        loanRecords.add(loanRecord);
    }

    public void addBillRecord(String billRecord) {
        billRecords.add(billRecord);
    }

    public void addWorkHistoryEntry(String entry) {
        workHistory.add(entry);
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, TERMINATED, ON_LEAVE
    }
}
