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

    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();
        
        if (orders == null || orders.isEmpty()) {
            showNoOrders();
            return;
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Order order : orders) {
            String orderId = "Order #" + order.getOrderId();
            String orderDate = order.getOrderDate().format(dateFormatter);
            String status = order.getOrderStatus().toString();
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
            
            VBox orderCard = createOrderCard(orderId, orderDate, status, amount, itemsSummary.toString());
            ordersContainer.getChildren().add(orderCard);
        }
    }

    private VBox createOrderCard(String orderId, String date, String status, String amount, String items) {
        VBox orderCard = new VBox(10);
        orderCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-border-color: #dee2e6; -fx-border-radius: 10; -fx-border-width: 1;");
        
        // Header row with order ID and status
        HBox headerRow = new HBox(10);
        headerRow.setStyle("-fx-alignment: center-left;");
        
        Label orderIdLabel = new Label(orderId);
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label statusLabel = new Label(status);
        String statusColor = getStatusColor(status);
        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 12px;");
        
        headerRow.getChildren().addAll(orderIdLabel, statusLabel);
        
        // Date row
        Label dateLabel = new Label("Order Date: " + date);
        dateLabel.setStyle("-fx-text-fill: #6c757d;");
        
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
        
        orderCard.getChildren().addAll(headerRow, dateLabel, itemsLabel, amountLabel, buttonsRow);
        
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

    private void showOrderDetails(String orderId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText(orderId);
        alert.setContentText("Detailed order information will be displayed here.\n\nThis feature will be fully implemented when connected to the database.");
        alert.showAndWait();
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
