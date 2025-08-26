package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderItem;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderHistoryRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderHistoryResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderCancellationRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderCancellationResponse;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class OrderHistoryController implements Initializable {

    @FXML
    private Button backButton;

    @FXML
    private Label userNameLabel;

    @FXML
    private VBox ordersContainer;

    @FXML
    private VBox noOrdersPanel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label errorLabel;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            EventBus.getDefault().register(this);
            
            currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                loadOrderHistory();
            } else {
                showError("Error: User not logged in");
            }
            
            System.out.println("OrderHistoryController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error in OrderHistoryController initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("userDetails");
        } catch (IOException e) {
            showError("Error navigating back: " + e.getMessage());
        }
    }

    @FXML
    void goToCatalogue(ActionEvent event) {
        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("catalogue");
        } catch (IOException e) {
            showError("Error navigating to catalogue: " + e.getMessage());
        }
    }

    private void loadOrderHistory() {
        try {
            // Show loading indicator
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }
            
            // Clear existing content
            ordersContainer.getChildren().clear();
            
            // Hide no orders message
            if (noOrdersPanel != null) {
                noOrdersPanel.setVisible(false);
            }
            
            System.out.println("Requesting order history for user: " + currentUser.getUserId());
            
            // Send request to server for real order history
            OrderHistoryRequest request = new OrderHistoryRequest(currentUser.getUserId());
            SimpleClient.getClient().sendToServer(request);
            
        } catch (Exception e) {
            showError("Error loading order history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onOrderHistoryResponse(OrderHistoryResponse response) {
        Platform.runLater(() -> {
            // Hide loading indicator
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            
            if (response.isSuccess()) {
                displayOrders(response.getOrders());
            } else {
                showError("Failed to load orders: " + response.getMessage());
            }
        });
    }

    @Subscribe
    public void onOrderCancellationResponse(OrderCancellationResponse response) {
        System.out.println("OrderHistoryController: Received cancellation response - Success: " + response.isSuccess() + ", Message: " + response.getMessage());
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                System.out.println("OrderHistoryController: Processing successful cancellation response");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Cancelled");
                alert.setHeaderText("Cancellation Successful");
                alert.setContentText(response.getMessage());
                alert.showAndWait();
                
                System.out.println("OrderHistoryController: Refreshing order history after cancellation");
                // Refresh the order history to show updated status
                loadOrderHistory();
            } else {
                System.out.println("OrderHistoryController: Processing failed cancellation response");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Cancellation Failed");
                alert.setHeaderText("Unable to Cancel Order");
                alert.setContentText(response.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();
        
        if (orders == null || orders.isEmpty()) {
            showNoOrders();
            return;
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        System.out.println("Displaying " + orders.size() + " orders");
        
        for (Order order : orders) {
            System.out.println("Displaying order: " + order.getOrderId());
            String orderId = "Order #" + order.getOrderId();
            String orderDate = order.getOrderDate().format(dateFormatter);
            String deliveryDate = order.getRequestedDeliveryDate().format(dateFormatter);
            String status = order.getOrderStatus().toString();
            String priority = order.getOrderPriority() != null ? order.getOrderPriority().toString() : "SCHEDULED";
            String amount = String.format("%.2f", order.getFinalAmount());
            
            // Create items summary
            StringBuilder itemsSummary = new StringBuilder();
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (int i = 0; i < order.getOrderItems().size(); i++) {
                    OrderItem item = order.getOrderItems().get(i);
                    if (i > 0) itemsSummary.append(", ");
                    itemsSummary.append(item.getItem().getName());
                    if (item.getQuantity() > 1) {
                        itemsSummary.append(" (x").append(item.getQuantity()).append(")");
                    }
                }
            } else {
                itemsSummary.append("No items");
            }
            
            VBox orderCard = createOrderCard(order, orderId, orderDate, deliveryDate, status, priority, amount, itemsSummary.toString());
            ordersContainer.getChildren().add(orderCard);
        }
    }

    private VBox createOrderCard(Order order, String orderId, String orderDate, String deliveryDate, String status, String priority, String amount, String items) {
        VBox orderCard = new VBox(10);
        orderCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-border-color: #dee2e6; -fx-border-radius: 10; -fx-border-width: 1;");
        
        // Header row with order ID, status, and priority
        HBox headerRow = new HBox(10);
        headerRow.setStyle("-fx-alignment: center-left;");
        
        Label orderIdLabel = new Label(orderId);
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label statusLabel = new Label(status);
        String statusColor = getStatusColor(status);
        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 12px;");
        
        Label priorityLabel = new Label(priority);
        String priorityColor = getPriorityColor(priority);
        priorityLabel.setStyle("-fx-background-color: " + priorityColor + "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 12px;");
        
        headerRow.getChildren().addAll(orderIdLabel, statusLabel, priorityLabel);
        
        // Date rows
        Label orderDateLabel = new Label("Order Date: " + orderDate);
        orderDateLabel.setStyle("-fx-text-fill: #6c757d;");
        
        Label deliveryDateLabel = new Label("Delivery Date: " + deliveryDate);
        deliveryDateLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
        
        // Items row
        Label itemsLabel = new Label("Items: " + items);
        itemsLabel.setStyle("-fx-font-size: 14px;");
        
        // Amount row
        Label amountLabel = new Label("Total: $" + amount);
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2E8B57;");
        
        // Action buttons
        HBox buttonsRow = new HBox(10);
        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 5 15;");
        viewDetailsButton.setOnAction(e -> showOrderDetails(orderId));
        
        buttonsRow.getChildren().add(viewDetailsButton);
        
        // Add cancel button if order can be cancelled
        if (order.canBeCancelled()) {
            Button cancelButton = new Button("Cancel Order");
            cancelButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5 15;");
            cancelButton.setOnAction(e -> cancelOrder(order));
            buttonsRow.getChildren().add(cancelButton);
        }
        
        orderCard.getChildren().addAll(headerRow, orderDateLabel, deliveryDateLabel, itemsLabel, amountLabel, buttonsRow);
        
        return orderCard;
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "delivered":
                return "#28a745";
            case "processing":
                return "#ffc107";
            case "shipped":
                return "#007bff";
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

    private void cancelOrder(Order order) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Order");
        confirmAlert.setHeaderText("Are you sure you want to cancel Order #" + order.getOrderId() + "?");
        
        // Calculate potential refund for display
        java.time.LocalDateTime currentTime = java.time.LocalDateTime.now();
        java.time.LocalDateTime deliveryTime = order.getRequestedDeliveryDate();
        long hoursUntilDelivery = java.time.temporal.ChronoUnit.HOURS.between(currentTime, deliveryTime);
        
        String refundInfo;
        if (hoursUntilDelivery >= 3) {
            refundInfo = "You will receive a 100% refund (≥ 3 hours before delivery)";
        } else if (hoursUntilDelivery >= 1) {
            refundInfo = "You will receive a 50% refund (1-3 hours before delivery)";
        } else {
            refundInfo = "No refund available (< 1 hour before delivery)";
        }
        
        confirmAlert.setContentText(refundInfo + "\n\nThis action cannot be undone.");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Send cancellation request to server
                    OrderCancellationRequest request = new OrderCancellationRequest(
                        order.getOrderId(), 
                        currentUser.getUserId(), 
                        "Cancelled by customer from order history"
                    );
                    SimpleClient.getClient().sendToServer(request);
                } catch (Exception e) {
                    showError("Error sending cancellation request: " + e.getMessage());
                }
            }
        });
    }

    private void showOrderDetails(String orderId) {
        try {
            // Extract order ID number from "Order #X" format
            Long orderIdLong = Long.parseLong(orderId.replace("Order #", ""));
            
            // Set the order ID for the OrderDetailsController
            OrderDetailsController.setOrderId(orderIdLong);
            
            // Unregister from EventBus and navigate to order details view
            EventBus.getDefault().unregister(this);
            App.setRoot("orderDetails");
            
        } catch (NumberFormatException e) {
            showError("Invalid order ID format: " + orderId);
        } catch (IOException e) {
            showError("Error navigating to order details: " + e.getMessage());
        } catch (Exception e) {
            showError("Error showing order details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showNoOrders() {
        ordersContainer.getChildren().clear();
        noOrdersPanel.setVisible(true);
        noOrdersPanel.setManaged(true);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setVisible(true);
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
