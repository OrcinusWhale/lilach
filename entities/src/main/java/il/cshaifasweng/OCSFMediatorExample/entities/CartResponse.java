package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class CartResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Cart cart;
    private List<CartItem> cartItems;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private int totalItemCount;

    public CartResponse() {}

    public CartResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CartResponse(boolean success, String message, Cart cart) {
        this(success, message);
        this.cart = cart;
        if (cart != null) {
            this.cartItems = cart.getCartItems();
            this.totalAmount = cart.getTotalAmount();
            this.discountAmount = cart.getDiscountAmount();
            this.finalAmount = cart.getFinalAmount();
            this.totalItemCount = cart.getTotalItemCount();
        }
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public int getTotalItemCount() { return totalItemCount; }
    public void setTotalItemCount(int totalItemCount) { this.totalItemCount = totalItemCount; }
}
