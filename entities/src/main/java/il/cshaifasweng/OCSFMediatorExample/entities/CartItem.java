package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart_items")
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    // Default constructor
    public CartItem() {}

    // Constructor
    public CartItem(Cart cart, Item item, int quantity) {
        this.cart = cart;
        this.item = item;
        this.quantity = quantity;
    }

    public CartItem(Cart cart, Item item, int quantity, String specialRequests) {
        this(cart, item, quantity);
        this.specialRequests = specialRequests;
    }

    // Getters and Setters
    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    // Helper methods
    public double getPrice() {
        if (item == null) return 0.0;
        // Use sale price if available, otherwise regular price
        return (item.getSalePrice() > 0) ? item.getSalePrice() : item.getPrice();
    }

    public double getSubtotal() {
        return getPrice() * quantity;
    }

    public String getItemName() {
        return item != null ? item.getName() : "Unknown Item";
    }

    public String getItemType() {
        return item != null ? item.getType() : "Unknown Type";
    }

    @Override
    public String toString() {
        return quantity + "x " + getItemName() + " - $" + String.format("%.2f", getSubtotal());
    }
}
