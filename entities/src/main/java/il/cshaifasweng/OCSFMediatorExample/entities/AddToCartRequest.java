package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class AddToCartRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private int itemId;
    private int quantity;
    private String specialRequests;

    public AddToCartRequest() {}

    public AddToCartRequest(int userId, int itemId, int quantity) {
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public AddToCartRequest(int userId, int itemId, int quantity, String specialRequests) {
        this(userId, itemId, quantity);
        this.specialRequests = specialRequests;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
}
