package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class ComplaintRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerEmail;
    private String description;
    private String orderNumber;

    // Default constructor
    public ComplaintRequest() {}

    // All-args constructor
    public ComplaintRequest(String customerEmail, String description) {
        this.customerEmail = customerEmail;
        this.description = description;
    }

    // Constructor with order number
    public ComplaintRequest(String customerEmail, String description, String orderNumber) {
        this.customerEmail = customerEmail;
        this.description = description;
        this.orderNumber = orderNumber;
    }

    // Getters and Setters
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
