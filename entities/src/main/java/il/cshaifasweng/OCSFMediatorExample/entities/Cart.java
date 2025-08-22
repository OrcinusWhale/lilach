package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Default constructor
    public Cart() {
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    // Constructor
    public Cart(User user) {
        this();
        this.user = user;
    }

    // Getters and Setters
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public void addCartItem(CartItem cartItem) {
        // Check if item already exists in cart
        CartItem existingItem = findCartItemByItem(cartItem.getItem());
        if (existingItem != null) {
            // Update quantity of existing item
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
        } else {
            // Add new item to cart
            cartItems.add(cartItem);
            cartItem.setCart(this);
        }
        updateLastModified();
    }

    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCart(null);
        updateLastModified();
    }

    public void updateCartItemQuantity(Item item, int newQuantity) {
        CartItem cartItem = findCartItemByItem(item);
        if (cartItem != null) {
            if (newQuantity <= 0) {
                removeCartItem(cartItem);
            } else {
                cartItem.setQuantity(newQuantity);
                updateLastModified();
            }
        }
    }

    public CartItem findCartItemByItem(Item item) {
        return cartItems.stream()
                .filter(cartItem -> cartItem.getItem().getItemId() == item.getItemId())
                .findFirst()
                .orElse(null);
    }

    public void clearCart() {
        cartItems.clear();
        updateLastModified();
    }

    public double getTotalAmount() {
        return cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public double getDiscountAmount() {
        if (user != null) {
            return user.calculateDiscount(getTotalAmount());
        }
        return 0.0;
    }

    public double getFinalAmount() {
        return getTotalAmount() - getDiscountAmount();
    }

    public int getTotalItemCount() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Cart #" + cartId + " - " + user.getFullName() + " (" + getTotalItemCount() + " items)";
    }
}
