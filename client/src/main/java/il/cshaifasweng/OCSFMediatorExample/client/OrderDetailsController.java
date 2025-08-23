package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OrderDetailsController implements Initializable {

    @FXML
    private Button backButton;

    @FXML
    private Label orderIdLabel;

    @FXML
    private Label orderStatusLabel;

    @FXML
    private Label orderPriorityLabel;

    @FXML
    private Label orderDateLabel;

    @FXML
    private Label deliveryDateLabel;

    @FXML
    private Label orderTypeLabel;

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerPhoneLabel;

    @FXML
    private Label storeNameLabel;

    @FXML
    private Label deliveryAddressLabel;

    @FXML
    private Label recipientNameLabel;

    @FXML
    private Label recipientPhoneLabel;

    @FXML
    private VBox deliveryInfoSection;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label deliveryFeeLabel;

    @FXML
    private Label finalTotalLabel;

    @FXML
    private TextArea greetingCardArea;

    @FXML
    private TextArea specialInstructionsArea;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private VBox errorPanel;

    @FXML
    private Label errorLabel;

    private Long orderId;
    private User currentUser;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    public static void setOrderId(Long orderId) {
        OrderDetailsController.staticOrderId = orderId;
    }
    private static Long staticOrderId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            EventBus.getDefault().register(this);
            
            currentUser = UserSession.getCurrentUser();
            if (currentUser == null) {
                showError("Error: User not logged in");
                return;
            }

            // Get order ID from static field (set by navigation)
            this.orderId = staticOrderId;
            
            if (orderId == null) {
                showError("Error: No order selected");
                return;
            }

            loadOrderDetails();
            
        } catch (Exception e) {
            System.err.println("Error in OrderDetailsController initialize: " + e.getMessage());
            e.printStackTrace();
            showError("Error initializing order details: " + e.getMessage());
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("orderHistory");
        } catch (IOException e) {
            showError("Error navigating back: " + e.getMessage());
        }
    }

    private void loadOrderDetails() {
        try {
            showLoading(true);
            
            // Use reflection to create OrderDetailsRequest to avoid direct class reference
            Class<?> requestClass = Class.forName("il.cshaifasweng.OCSFMediatorExample.entities.OrderDetailsRequest");
            Object request = requestClass.getConstructor(Long.class, int.class).newInstance(orderId, currentUser.getUserId());
            App.getClient().sendToServer(request);
            
        } catch (Exception e) {
            showError("Error loading order details: " + e.getMessage());
        }
    }

    @Subscribe
    public void handleMessage(Object message) {
        if (message.getClass().getSimpleName().equals("OrderDetailsResponse")) {
            Platform.runLater(() -> {
                showLoading(false);
                
                try {
                    // Use reflection to avoid direct class reference
                    boolean success = (Boolean) message.getClass().getMethod("isSuccess").invoke(message);
                    if (success) {
                        Order order = (Order) message.getClass().getMethod("getOrder").invoke(message);
                        displayOrderDetails(order);
                    } else {
                        String errorMessage = (String) message.getClass().getMethod("getMessage").invoke(message);
                        showError("Failed to load order details: " + errorMessage);
                    }
                } catch (Exception e) {
                    showError("Error processing order details response: " + e.getMessage());
                }
            });
        }
    }

    private void displayOrderDetails(Order order) {
        // Header information
        orderIdLabel.setText("Order #" + order.getOrderId());
        orderStatusLabel.setText(order.getOrderStatus().toString());
        orderStatusLabel.setStyle("-fx-background-color: " + getStatusColor(order.getOrderStatus().toString()) + 
                                 "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;");

        orderPriorityLabel.setText(order.getOrderPriority().toString());
        orderPriorityLabel.setStyle("-fx-background-color: " + getPriorityColor(order.getOrderPriority().toString()) + 
                                   "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;");

        orderDateLabel.setText(order.getOrderDate().format(dateFormatter));
        deliveryDateLabel.setText(order.getRequestedDeliveryDate().format(dateFormatter));
        orderTypeLabel.setText(order.getOrderType().toString());

        // Customer information
        customerNameLabel.setText(order.getUser().getFullName());
        customerPhoneLabel.setText(order.getUser().getPhone() != null ? order.getUser().getPhone() : "Not provided");
        
        // Store information
        storeNameLabel.setText(order.getStore().getStoreName() + " - " + order.getStore().getAddress());

        // Delivery information (show only for delivery orders)
        if (order.isDelivery()) {
            deliveryInfoSection.setVisible(true);
            deliveryInfoSection.setManaged(true);
            deliveryAddressLabel.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Not provided");
            recipientNameLabel.setText(order.getRecipientName() != null ? order.getRecipientName() : "Not provided");
            recipientPhoneLabel.setText(order.getRecipientPhone() != null ? order.getRecipientPhone() : "Not provided");
        } else {
            deliveryInfoSection.setVisible(false);
            deliveryInfoSection.setManaged(false);
        }

        // Order items
        displayOrderItems(order);

        // Financial information
        subtotalLabel.setText(String.format("$%.2f", order.getTotalAmount()));
        discountLabel.setText(String.format("-$%.2f", order.getDiscountAmount()));
        deliveryFeeLabel.setText(String.format("$%.2f", order.getDeliveryFee()));
        finalTotalLabel.setText(String.format("$%.2f", order.getFinalAmount()));

        // Additional information
        if (order.getGreetingCardMessage() != null && !order.getGreetingCardMessage().trim().isEmpty()) {
            greetingCardArea.setText(order.getGreetingCardMessage());
            greetingCardArea.setVisible(true);
        } else {
            greetingCardArea.setVisible(false);
        }

        if (order.getSpecialInstructions() != null && !order.getSpecialInstructions().trim().isEmpty()) {
            specialInstructionsArea.setText(order.getSpecialInstructions());
            specialInstructionsArea.setVisible(true);
        } else {
            specialInstructionsArea.setVisible(false);
        }
    }

    private void displayOrderItems(Order order) {
        orderItemsContainer.getChildren().clear();

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            Label noItemsLabel = new Label("No items found");
            noItemsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            orderItemsContainer.getChildren().add(noItemsLabel);
            return;
        }

        for (OrderItem item : order.getOrderItems()) {
            VBox itemCard = createItemCard(item);
            orderItemsContainer.getChildren().add(itemCard);
        }
    }

    private VBox createItemCard(OrderItem orderItem) {
        VBox itemCard = new VBox(5);
        itemCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label itemNameLabel = new Label(orderItem.getItem().getName());
        itemNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label quantityLabel = new Label("Quantity: " + orderItem.getQuantity());
        
        Label priceLabel = new Label(String.format("Unit Price: $%.2f", orderItem.getPrice()));
        
        Label subtotalLabel = new Label(String.format("Subtotal: $%.2f", orderItem.getPrice() * orderItem.getQuantity()));
        subtotalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E8B57;");

        itemCard.getChildren().addAll(itemNameLabel, quantityLabel, priceLabel, subtotalLabel);

        if (orderItem.getSpecialRequests() != null && !orderItem.getSpecialRequests().trim().isEmpty()) {
            Label specialRequestsLabel = new Label("Special Requests: " + orderItem.getSpecialRequests());
            specialRequestsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            itemCard.getChildren().add(specialRequestsLabel);
        }

        return itemCard;
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "delivered":
                return "#28a745";
            case "confirmed":
                return "#007bff";
            case "preparing":
                return "#ffc107";
            case "ready_for_pickup":
                return "#17a2b8";
            case "out_for_delivery":
                return "#fd7e14";
            case "picked_up":
                return "#28a745";
            case "cancelled":
                return "#dc3545";
            default:
                return "#6c757d";
        }
    }

    private String getPriorityColor(String priority) {
        switch (priority.toLowerCase()) {
            case "immediate":
                return "#ff6b6b"; // Red for immediate orders
            case "scheduled":
                return "#4ecdc4"; // Teal for scheduled orders
            default:
                return "#6c757d";
        }
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorPanel.setVisible(true);
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Order Details Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Subscribe
    public void unsubscribe(UnsubscribeEvent event) {
        EventBus.getDefault().unregister(this);
    }
}
