package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Employee;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class DataInitializer {
    
    /**
     * Initialize sample data for testing the authentication system
     */
    public static void initializeSampleData() {
        try {
            // Check if admin user already exists
            if (!userExists("admin")) {
                createAdminUser();
            }
            
            // Check if sample employee exists
            if (!userExists("employee1")) {
                createSampleEmployee();
            }
            
            // Check if sample customer exists
            if (!userExists("customer1")) {
                createSampleCustomer();
            }
            
            System.out.println("Sample data initialization completed.");
            
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createAdminUser() {
        try {
            App.session.beginTransaction();
            
            // Create admin user
            User adminUser = new User(
                "admin",
                "admin123", // Will be hashed by AuthenticationService
                "System",
                "Administrator",
                "admin@lilach.com",
                "123-456-7890",
                "123 Admin Street, Admin City"
            );
            adminUser.setUserType(User.UserType.ADMIN);
            
            // Hash password
            String hashedPassword = AuthenticationService.hashPassword(adminUser.getPassword());
            adminUser.setPassword(hashedPassword);
            
            App.session.save(adminUser);
            App.session.flush();
            App.session.getTransaction().commit();
            
            System.out.println("Admin user created: username=admin, password=admin123");
            
        } catch (Exception e) {
            if (App.session.getTransaction().isActive()) {
                App.session.getTransaction().rollback();
            }
            System.err.println("Error creating admin user: " + e.getMessage());
        }
    }
    
    private static void createSampleEmployee() {
        try {
            App.session.beginTransaction();
            
            // Create employee user
            User employeeUser = new User(
                "employee1",
                "emp123", // Will be hashed
                "John",
                "Employee",
                "john.employee@lilach.com",
                "123-456-7891",
                "456 Employee Street, Employee City"
            );
            employeeUser.setUserType(User.UserType.EMPLOYEE);
            
            // Hash password
            String hashedPassword = AuthenticationService.hashPassword(employeeUser.getPassword());
            employeeUser.setPassword(hashedPassword);
            
            App.session.save(employeeUser);
            App.session.flush();
            
            // Create employee data
            Employee employee = new Employee(
                employeeUser,
                "EMP001",
                "Sales",
                "Sales Representative"
            );
            employee.setSalary(50000.0);
            employee.setStatus(Employee.EmployeeStatus.ACTIVE);
            
            App.session.save(employee);
            App.session.flush();
            App.session.getTransaction().commit();
            
            System.out.println("Sample employee created: username=employee1, password=emp123");
            
        } catch (Exception e) {
            if (App.session.getTransaction().isActive()) {
                App.session.getTransaction().rollback();
            }
            System.err.println("Error creating sample employee: " + e.getMessage());
        }
    }
    
    private static void createSampleCustomer() {
        try {
            App.session.beginTransaction();
            
            // Create customer user
            User customerUser = new User(
                "customer1",
                "cust123", // Will be hashed
                "Jane",
                "Customer",
                "jane.customer@example.com",
                "123-456-7892",
                "789 Customer Street, Customer City"
            );
            customerUser.setUserType(User.UserType.CUSTOMER);
            
            // Hash password
            String hashedPassword = AuthenticationService.hashPassword(customerUser.getPassword());
            customerUser.setPassword(hashedPassword);
            
            App.session.save(customerUser);
            App.session.flush();
            App.session.getTransaction().commit();
            
            System.out.println("Sample customer created: username=customer1, password=cust123");
            
        } catch (Exception e) {
            if (App.session.getTransaction().isActive()) {
                App.session.getTransaction().rollback();
            }
            System.err.println("Error creating sample customer: " + e.getMessage());
        }
    }
    
    private static boolean userExists(String username) {
        try {
            CriteriaBuilder builder = App.session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<User> root = query.from(User.class);
            query.select(builder.count(root));
            query.where(builder.equal(root.get("username"), username));
            
            Long count = App.session.createQuery(query).getSingleResult();
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }
}
