package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private SessionFactory sessionFactory;

    public OrderService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // Cart Management Methods
    public CartResponse addToCart(AddToCartRequest request) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Get user and item
            User user = session.get(User.class, request.getUserId());
            if (user == null) {
                return new CartResponse(false, "User not found");
            }

            Item item = session.get(Item.class, request.getItemId());
            if (item == null) {
                return new CartResponse(false, "Item not found");
            }

            // Check if user can place orders
            if (!user.canPlaceOrders()) {
                return new CartResponse(false, "User is not authorized to place orders. Please setup subscription first.");
            }

            // Get or create active cart for user
            Cart cart = getActiveCartForUser(session, user);
            if (cart == null) {
                cart = new Cart(user);
                session.save(cart);
            }

            // Create cart item
            CartItem cartItem = new CartItem(cart, item, request.getQuantity(), request.getSpecialRequests());
            
            // Check if item already exists in cart
            CartItem existingItem = cart.findCartItemByItem(item);
            if (existingItem != null) {
                // Update quantity of existing item
                existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
                session.saveOrUpdate(existingItem); // Save the updated existing item
                System.out.println("OrderService: Updated existing cart item quantity to " + existingItem.getQuantity());
            } else {
                // Add new item to cart
                cart.addCartItem(cartItem);
                session.saveOrUpdate(cartItem); // Save the new cart item
                System.out.println("OrderService: Added new cart item with quantity " + cartItem.getQuantity());
            }
            
            session.saveOrUpdate(cart);
            session.flush(); // Force immediate database write
            transaction.commit();
            
            System.out.println("OrderService: Transaction committed successfully for addToCart");

            // Force initialization of cart items to avoid lazy loading issues
            cart.getCartItems().size(); // This triggers the lazy loading
            
            // Also ensure item details are loaded for each cart item
            for (CartItem ci : cart.getCartItems()) {
                if (ci.getItem() != null) {
                    ci.getItem().getName(); // Force item loading
                    ci.getItem().getPrice(); // Force price loading
                    ci.getItem().getType(); // Force type loading
                }
            }

            System.out.println("OrderService: Cart after adding item has " + cart.getCartItems().size() + " items");
            for (CartItem ci : cart.getCartItems()) {
                System.out.println("OrderService: Cart item - " + ci.getItemName() + 
                                 ", Qty: " + ci.getQuantity() + ", Price: $" + ci.getPrice());
            }

            return new CartResponse(true, "Item added to cart successfully", cart);

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return new CartResponse(false, "Error adding item to cart: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public CartResponse getCart(int userId) {
        Session session = sessionFactory.openSession();

        try {
            System.out.println("OrderService: Starting getCart for user ID: " + userId);
            User user = session.get(User.class, userId);
            if (user == null) {
                System.out.println("OrderService: User not found for ID: " + userId);
                return new CartResponse(false, "User not found");
            }

            Cart cart = getActiveCartForUser(session, user);
            if (cart == null) {
                System.out.println("OrderService: No active cart found for user " + userId + ", returning empty cart");
                return new CartResponse(true, "Cart is empty", new Cart(user));
            }

            // Force initialization of cart items to avoid lazy loading issues
            cart.getCartItems().size(); // This triggers the lazy loading
            
            // Also ensure item details are loaded for each cart item
            for (CartItem cartItem : cart.getCartItems()) {
                if (cartItem.getItem() != null) {
                    cartItem.getItem().getName(); // Force item loading
                    cartItem.getItem().getPrice(); // Force price loading
                    cartItem.getItem().getType(); // Force type loading
                }
            }

            System.out.println("OrderService: Retrieved cart has " + cart.getCartItems().size() + " items");
            for (CartItem ci : cart.getCartItems()) {
                System.out.println("OrderService: Retrieved cart item - " + ci.getItemName() + 
                                 ", Qty: " + ci.getQuantity() + ", Price: $" + ci.getPrice());
            }

            CartResponse response = new CartResponse(true, "Cart retrieved successfully", cart);
            System.out.println("OrderService: Returning CartResponse with " + 
                             (response.getCartItems() != null ? response.getCartItems().size() : 0) + " items");
            return response;

        } catch (Exception e) {
            System.err.println("OrderService: Error in getCart: " + e.getMessage());
            e.printStackTrace();
            return new CartResponse(false, "Error retrieving cart: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public CartResponse updateCartItemQuantity(int userId, int itemId, int newQuantity) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user == null) {
                return new CartResponse(false, "User not found");
            }

            Item item = session.get(Item.class, itemId);
            if (item == null) {
                return new CartResponse(false, "Item not found");
            }

            Cart cart = getActiveCartForUser(session, user);
            if (cart == null) {
                return new CartResponse(false, "Cart not found");
            }

            // Handle item removal (quantity = 0) vs update
            if (newQuantity <= 0) {
                CartItem cartItemToRemove = cart.findCartItemByItem(item);
                if (cartItemToRemove != null) {
                    System.out.println("OrderService: Removing cart item - " + cartItemToRemove.getItemName());
                    cart.removeCartItem(cartItemToRemove);
                    session.delete(cartItemToRemove); // Explicitly delete from database
                    session.saveOrUpdate(cart);
                }
            } else {
                cart.updateCartItemQuantity(item, newQuantity);
                session.saveOrUpdate(cart);
            }
            transaction.commit();

            // Force initialization after update
            cart.getCartItems().size();
            for (CartItem ci : cart.getCartItems()) {
                if (ci.getItem() != null) {
                    ci.getItem().getName();
                    ci.getItem().getPrice();
                    ci.getItem().getType();
                }
            }

            System.out.println("OrderService: Updated cart has " + cart.getCartItems().size() + " items");

            return new CartResponse(true, "Cart updated successfully", cart);

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return new CartResponse(false, "Error updating cart: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public CartResponse clearCart(int userId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user == null) {
                return new CartResponse(false, "User not found");
            }

            Cart cart = getActiveCartForUser(session, user);
            if (cart != null) {
                System.out.println("OrderService: Clearing cart with " + cart.getCartItems().size() + " items");
                
                // Delete all cart items from database first
                for (CartItem cartItem : cart.getCartItems()) {
                    System.out.println("OrderService: Deleting cart item - " + cartItem.getItemName());
                    session.delete(cartItem);
                }
                
                // Clear the collection
                cart.clearCart();
                session.saveOrUpdate(cart);
                System.out.println("OrderService: Cart cleared successfully");
            }

            transaction.commit();
            return new CartResponse(true, "Cart cleared successfully", cart);

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return new CartResponse(false, "Error clearing cart: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // Order Management Methods
    public OrderResponse createOrder(CreateOrderRequest request) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Get user and validate
            User user = session.get(User.class, request.getUserId());
            if (user == null) {
                return new OrderResponse(false, "User not found");
            }

            if (!user.canPlaceOrders()) {
                return new OrderResponse(false, "User is not authorized to place orders. Please setup subscription first.");
            }

            // Get store
            Store store = session.get(Store.class, request.getStoreId());
            if (store == null) {
                return new OrderResponse(false, "Store not found");
            }

            // Validate store access for store-specific users
            if (user.isStoreSpecific() && !user.getStore().getStoreId().equals(store.getStoreId())) {
                return new OrderResponse(false, "Store-specific users can only order from their assigned store");
            }

            // Get user's cart
            Cart cart = getActiveCartForUser(session, user);
            if (cart == null || cart.isEmpty()) {
                return new OrderResponse(false, "Cart is empty. Please add items before placing order.");
            }

            // Validate delivery date
            if (request.getRequestedDeliveryDate().isBefore(LocalDateTime.now())) {
                return new OrderResponse(false, "Delivery date must be in the future");
            }

            // Create order
            Order.OrderType orderType = Order.OrderType.valueOf(request.getOrderType().toUpperCase());
            Order order = new Order(user, store, orderType, request.getRequestedDeliveryDate(), request.getCreatedVia());

            // Set optional fields
            if (request.getDeliveryAddress() != null && !request.getDeliveryAddress().trim().isEmpty()) {
                order.setDeliveryAddress(request.getDeliveryAddress());
            } else if (orderType == Order.OrderType.DELIVERY) {
                order.setDeliveryAddress(user.getAddress()); // Use user's default address
            }

            if (request.getGreetingCardMessage() != null && !request.getGreetingCardMessage().trim().isEmpty()) {
                order.setGreetingCardMessage(request.getGreetingCardMessage());
            }

            if (request.getSpecialInstructions() != null && !request.getSpecialInstructions().trim().isEmpty()) {
                order.setSpecialInstructions(request.getSpecialInstructions());
            }

            // Convert cart items to order items
            for (CartItem cartItem : cart.getCartItems()) {
                OrderItem orderItem = new OrderItem(order, cartItem.getItem(), cartItem.getQuantity(), cartItem.getSpecialRequests());
                order.addOrderItem(orderItem);
            }

            // Save order
            session.save(order);

            // Clear cart after successful order creation
            cart.clearCart();
            session.saveOrUpdate(cart);

            transaction.commit();

            return new OrderResponse(true, "Order created successfully. Order ID: " + order.getOrderId(), order);

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return new OrderResponse(false, "Error creating order: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public List<Order> getUserOrders(int userId) {
        Session session = sessionFactory.openSession();

        try {
            Query<Order> query = session.createQuery(
                "FROM Order o WHERE o.user.userId = :userId ORDER BY o.orderDate DESC", Order.class);
            query.setParameter("userId", userId);
            return query.list();

        } catch (Exception e) {
            System.err.println("Error retrieving user orders: " + e.getMessage());
            return null;
        } finally {
            session.close();
        }
    }

    public Order getOrderById(Long orderId) {
        Session session = sessionFactory.openSession();

        try {
            return session.get(Order.class, orderId);
        } catch (Exception e) {
            System.err.println("Error retrieving order: " + e.getMessage());
            return null;
        } finally {
            session.close();
        }
    }

    public boolean updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Order order = session.get(Order.class, orderId);
            if (order == null) {
                return false;
            }

            order.setOrderStatus(newStatus);
            session.saveOrUpdate(order);
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        } finally {
            session.close();
        }
    }

    // Helper Methods
    private Cart getActiveCartForUser(Session session, User user) {
        try {
            Query<Cart> query = session.createQuery(
                "FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.item WHERE c.user.userId = :userId AND c.isActive = true", Cart.class);
            query.setParameter("userId", user.getUserId());
            List<Cart> carts = query.list();
            
            Cart cart = carts.isEmpty() ? null : carts.get(0);
            if (cart != null) {
                System.out.println("OrderService: Found active cart for user " + user.getUserId() + 
                                 " with " + cart.getCartItems().size() + " items");
            } else {
                System.out.println("OrderService: No active cart found for user " + user.getUserId());
            }
            return cart;
        } catch (Exception e) {
            System.err.println("Error retrieving cart: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
