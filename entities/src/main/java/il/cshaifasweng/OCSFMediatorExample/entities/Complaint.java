package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "complaints")
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "assigned_employee")
    private String assignedEmployee;

    @Column(name = "compensation_amount", precision = 12, scale = 2)
    private BigDecimal compensationAmount;

    @Column(name = "compensation_currency", length = 3)
    private String compensationCurrency;

    @Column(name = "decision_at")
    private Instant decisionAt;

    @Column(name = "decision_note", columnDefinition = "TEXT")
    private String decisionNote;

    @Column(name = "deadline")
    private Instant deadline;

    @Column(name = "order_number")
    private String orderNumber;

    // Default constructor
    public Complaint() {
        this.createdAt = Instant.now();
        this.deadline = Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS);
        this.status = "PENDING";
    }

    // All-args constructor
    public Complaint(String customerEmail, String description, Instant createdAt, String status) {
        this.customerEmail = customerEmail;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.status = status != null ? status : "PENDING";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(String assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public BigDecimal getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(BigDecimal compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public String getCompensationCurrency() {
        return compensationCurrency;
    }

    public void setCompensationCurrency(String compensationCurrency) {
        this.compensationCurrency = compensationCurrency;
    }

    public Instant getDecisionAt() {
        return decisionAt;
    }

    public void setDecisionAt(Instant decisionAt) {
        this.decisionAt = decisionAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", customerEmail='" + customerEmail + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", assignedEmployee='" + assignedEmployee + '\'' +
                ", compensationAmount=" + compensationAmount +
                ", compensationCurrency='" + compensationCurrency + '\'' +
                ", decisionAt=" + decisionAt +
                ", decisionNote='" + decisionNote + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                '}';
    }
}
