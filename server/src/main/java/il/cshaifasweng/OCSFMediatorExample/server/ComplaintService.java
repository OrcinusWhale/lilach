package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComplaintService {
    private final SessionFactory sessionFactory;

    public ComplaintService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public ComplaintResponse submit(ComplaintRequest request) {
        // Validate input
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            return new ComplaintResponse(false, "Please enter a complaint description.", null);
        }

        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            System.out.println("✓ Session opened successfully");
        } catch (Exception sessionError) {
            System.err.println("✗ Failed to open Hibernate session:");
            sessionError.printStackTrace();
            return new ComplaintResponse(false, "Database connection error", null);
        }

        try {
            transaction = session.beginTransaction();

            // Create new complaint
            Complaint complaint = new Complaint();
            complaint.setCustomerEmail(request.getCustomerEmail());
            complaint.setDescription(request.getDescription().trim());
            complaint.setCreatedAt(Instant.now());
            complaint.setStatus("PENDING");

            // Persist to database
            System.out.println("Attempting to save complaint to database...");
            session.save(complaint);
            System.out.println("Complaint saved, flushing session...");
            session.flush();
            System.out.println("Session flushed, committing transaction...");
            transaction.commit();
            System.out.println("Transaction committed successfully");

            // Calculate ETA (24 hours from now)
            Instant eta = Instant.now().plusSeconds(24 * 60 * 60); // 24 hours
            String etaIso = eta.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT);

            System.out.println("Complaint submitted successfully - ID: " + complaint.getId() + 
                             ", Email: " + complaint.getCustomerEmail() + 
                             ", ETA: " + etaIso);

            return new ComplaintResponse(true, 
                "Your complaint has been received and will be handled within 24 hours.", 
                etaIso);

        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                    System.err.println("Transaction rolled back successfully");
                } catch (Exception rollbackError) {
                    System.err.println("Failed to rollback transaction: " + rollbackError.getMessage());
                }
            }
            
            System.err.println("=== COMPLAINT SUBMISSION ERROR ===");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Customer email: " + request.getCustomerEmail());
            System.err.println("Description length: " + (request.getDescription() != null ? request.getDescription().length() : "null"));
            
            System.err.println("=== FULL EXCEPTION STACK TRACE ===");
            e.printStackTrace();
            
            System.err.println("=== NESTED EXCEPTION ANALYSIS ===");
            Throwable cause = e.getCause();
            int level = 1;
            while (cause != null) {
                System.err.println("Level " + level + " - " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                if (cause.getMessage() != null && cause.getMessage().contains("SQL")) {
                    System.err.println("*** SQL ERROR DETECTED: " + cause.getMessage() + " ***");
                }
                cause = cause.getCause();
                level++;
            }
            
            return new ComplaintResponse(false, "Database error: " + e.getMessage(), null);
        } finally {
            session.close();
        }
    }

    /**
     * Assigns a complaint to an employee.
     * @param complaintId The ID of the complaint to assign
     * @param employeeUsername The username of the employee to assign to
     * @return ComplaintActionResponse indicating success or failure
     */
    public ComplaintActionResponse assign(int complaintId, String employeeUsername) {
        if (employeeUsername == null || employeeUsername.trim().isEmpty()) {
            return new ComplaintActionResponse(false, "Employee username cannot be empty");
        }

        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Find the complaint
            Complaint complaint = session.get(Complaint.class, complaintId);
            if (complaint == null) {
                return new ComplaintActionResponse(false, "Complaint not found with ID: " + complaintId);
            }

            // Check if already assigned
            if (complaint.getAssignedEmployee() != null) {
                return new ComplaintActionResponse(false, 
                    "Complaint is already assigned to: " + complaint.getAssignedEmployee());
            }

            // Assign the complaint
            complaint.setAssignedEmployee(employeeUsername.trim());
            complaint.setStatus("ASSIGNED");
            
            session.update(complaint);
            transaction.commit();

            System.out.println("Complaint " + complaintId + " assigned to " + employeeUsername);
            return new ComplaintActionResponse(true, 
                "Complaint successfully assigned to " + employeeUsername);

        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackError) {
                    System.err.println("Failed to rollback transaction: " + rollbackError.getMessage());
                }
            }
            
            System.err.println("Error assigning complaint " + complaintId + " to " + employeeUsername + ": " + e.getMessage());
            e.printStackTrace();
            return new ComplaintActionResponse(false, "Database error: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Makes a compensation decision on a complaint.
     * @param request The decision request containing complaint ID, employee, amount, currency, and note
     * @return ComplaintActionResponse indicating success or failure
     */
    public ComplaintActionResponse decide(ComplaintDecisionRequest request) {
        // Validate input
        if (request.getCompensationAmount() == null || request.getCompensationAmount().compareTo(BigDecimal.ZERO) < 0) {
            return new ComplaintActionResponse(false, "Compensation amount must be >= 0");
        }
        
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            return new ComplaintActionResponse(false, "Currency cannot be empty");
        }
        
        if (request.getEmployeeUsername() == null || request.getEmployeeUsername().trim().isEmpty()) {
            return new ComplaintActionResponse(false, "Employee username cannot be empty");
        }

        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Find the complaint
            Complaint complaint = session.get(Complaint.class, request.getComplaintId());
            if (complaint == null) {
                return new ComplaintActionResponse(false, "Complaint not found with ID: " + request.getComplaintId());
            }

            // Set assigned employee if not already set
            if (complaint.getAssignedEmployee() == null) {
                complaint.setAssignedEmployee(request.getEmployeeUsername().trim());
            }
            
            // Set compensation details
            complaint.setCompensationAmount(request.getCompensationAmount());
            complaint.setCompensationCurrency(request.getCurrency().trim());
            complaint.setDecisionAt(Instant.now());
            complaint.setDecisionNote(request.getNote() != null ? request.getNote().trim() : null);
            complaint.setStatus("HANDLED");
            
            session.update(complaint);
            transaction.commit();

            System.out.println("Compensation decision made for complaint " + request.getComplaintId() + 
                             " by " + request.getEmployeeUsername() + 
                             ": " + request.getCompensationAmount() + " " + request.getCurrency());
            
            return new ComplaintActionResponse(true, 
                "Compensation of " + request.getCompensationAmount() + " " + request.getCurrency() + 
                " has been approved for this complaint");

        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackError) {
                    System.err.println("Failed to rollback transaction: " + rollbackError.getMessage());
                }
            }
            
            System.err.println("Error making compensation decision for complaint " + request.getComplaintId() + ": " + e.getMessage());
            e.printStackTrace();
            return new ComplaintActionResponse(false, "Database error: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Retrieves all complaints for employee management.
     * @return List of all complaints
     */
    public List<Complaint> getAllComplaints() {
        Session session = null;
        
        try {
            session = sessionFactory.openSession();
            
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Complaint> query = builder.createQuery(Complaint.class);
            Root<Complaint> root = query.from(Complaint.class);
            query.select(root).orderBy(builder.desc(root.get("createdAt")));
            
            return session.createQuery(query).getResultList();
            
        } catch (Exception e) {
            System.err.println("Error retrieving complaints: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list on error
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
