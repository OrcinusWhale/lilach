package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private double price; // Price at time of order (may differ from current item price)

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    // Default constructor
    public OrderItem() {}

    // Constructor
    public OrderItem(Order order, Item item, int quantity) {
        this.order = order;
        this.item = item;
        this.quantity = quantity;
        // Use sale price if available, otherwise regular price
        this.price = (item.getSalePrice() > 0) ? item.getSalePrice() : item.getPrice();
    }

    public OrderItem(Order order, Item item, int quantity, String specialRequests) {
        this(order, item, quantity);
        this.specialRequests = specialRequests;
    }

    // Getters and Setters
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Item getItem() { return item; }
    public void setItem(Item item) { 
        this.item = item;
        // Update price when item changes
        if (item != null) {
            this.price = (item.getSalePrice() > 0) ? item.getSalePrice() : item.getPrice();
        }
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    // Helper methods
    public double getSubtotal() {
        return price * quantity;
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
