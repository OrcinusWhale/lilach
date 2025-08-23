package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CheckoutController implements Initializable {

    @FXML
    private Label orderSummaryLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label discountAmountLabel;

    @FXML
    private Label finalAmountLabel;

    @FXML
    private ComboBox<String> orderTypeComboBox;

    @FXML
    private ComboBox<Store> storeComboBox;

    @FXML
    private DatePicker deliveryDatePicker;

    @FXML
    private ComboBox<String> deliveryTimeComboBox;

    @FXML
    private TextField deliveryAddressField;

    @FXML
    private Label deliveryAddressLabel;

    @FXML
    private TextField recipientNameField;

    @FXML
    private Label recipientNameLabel;

    @FXML
    private TextField recipientPhoneField;

    @FXML
    private Label recipientPhoneLabel;

    @FXML
    private TextArea greetingCardTextArea;

    @FXML
    private TextArea specialInstructionsTextArea;

    @FXML
    private Button placeOrderButton;

    @FXML
    private Button backToCartButton;

    private Cart currentCart;
    private User currentUser;
    private List<Store> availableStores;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            EventBus.getDefault().register(this);
            System.out.println("CheckoutController: EventBus registered successfully");
            
            setupUI();
            System.out.println("CheckoutController: setupUI completed successfully");
            
            loadStores();
            System.out.println("CheckoutController: loadStores completed successfully");
            
            System.out.println("CheckoutController: initialized successfully");
        } catch (Exception e) {
            System.err.println("Error in CheckoutController initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        // Setup order type combo box
        orderTypeComboBox.setItems(FXCollections.observableArrayList("DELIVERY", "PICKUP"));
        orderTypeComboBox.setValue("DELIVERY");
        orderTypeComboBox.setOnAction(e -> {
            updateDeliveryFields();
            updateOrderSummary(); // Recalculate totals when order type changes
        });

        // Setup delivery time options
        deliveryTimeComboBox.setItems(FXCollections.observableArrayList(
            "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
        ));
        deliveryTimeComboBox.setValue("12:00");

        // Set minimum date to tomorrow
        deliveryDatePicker.setValue(java.time.LocalDate.now().plusDays(1));

        currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.getAddress() != null) {
            deliveryAddressField.setText(currentUser.getAddress());
        }

        updateDeliveryFields();
    }

    private void updateDeliveryFields() {
        boolean isDelivery = "DELIVERY".equals(orderTypeComboBox.getValue());
        deliveryAddressLabel.setVisible(isDelivery);
        deliveryAddressField.setVisible(isDelivery);
        recipientNameLabel.setVisible(isDelivery);
        recipientNameField.setVisible(isDelivery);
        recipientPhoneLabel.setVisible(isDelivery);
        recipientPhoneField.setVisible(isDelivery);
        
        if (!isDelivery) {
            deliveryAddressField.clear();
            recipientNameField.clear();
            recipientPhoneField.clear();
        } else if (currentUser != null) {
            if (currentUser.getAddress() != null) {
                deliveryAddressField.setText(currentUser.getAddress());
            }
            // Pre-fill recipient with user's name and phone if available
            if (currentUser.getFullName() != null) {
                recipientNameField.setText(currentUser.getFullName());
            }
            if (currentUser.getPhone() != null) {
                recipientPhoneField.setText(currentUser.getPhone());
            }
        }
    }

    private void loadStores() {
        try {
            App.getClient().sendToServer(new StoreListRequest());
        } catch (IOException e) {
            showError("Error loading stores: " + e.getMessage());
        }
    }

    @Subscribe
    public void onStoreListResponse(StoreListResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                availableStores = response.getStores();
                storeComboBox.setItems(FXCollections.observableArrayList(availableStores));
                
                // Select appropriate store based on user type
                if (currentUser != null) {
                    if (currentUser.isStoreSpecific() && currentUser.getStore() != null) {
                        // Store-specific users can only select their assigned store
                        storeComboBox.setValue(currentUser.getStore());
                        storeComboBox.setDisable(true);
                    } else {
                        // Brand users can select any store
                        if (!availableStores.isEmpty()) {
                            storeComboBox.setValue(availableStores.get(0));
                        }
                    }
                }
            } else {
                showError("Error loading stores: " + response.getMessage());
            }
        });
    }

    public void setCart(Cart cart) {
        this.currentCart = cart;
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        if (currentCart == null) return;

        StringBuilder summary = new StringBuilder();
        summary.append("Order Summary:\n\n");
        
        for (CartItem item : currentCart.getCartItems()) {
            summary.append(String.format("%dx %s - $%.2f\n", 
                item.getQuantity(), item.getItemName(), item.getSubtotal()));
        }

        orderSummaryLabel.setText(summary.toString());
        
        double deliveryFee = "DELIVERY".equals(orderTypeComboBox.getValue()) ? 15.0 : 0.0; // Fixed delivery fee
        double finalAmount = currentCart.getFinalAmount() + deliveryFee;
        
        totalAmountLabel.setText(String.format("$%.2f", currentCart.getTotalAmount()));
        discountAmountLabel.setText(String.format("$%.2f", currentCart.getDiscountAmount()));
        
        if (deliveryFee > 0) {
            finalAmountLabel.setText(String.format("$%.2f (includes $%.2f delivery fee)", finalAmount, deliveryFee));
        } else {
            finalAmountLabel.setText(String.format("$%.2f", finalAmount));
        }
    }

    @FXML
    void placeOrder(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            CreateOrderRequest request = buildOrderRequest();
            App.getClient().sendToServer(request);
            placeOrderButton.setDisable(true);
            placeOrderButton.setText("Placing Order...");
        } catch (Exception e) {
            showError("Error placing order: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (currentCart == null || currentCart.isEmpty()) {
            showError("Cart is empty");
            return false;
        }

        if (storeComboBox.getValue() == null) {
            showError("Please select a store");
            return false;
        }

        if (deliveryDatePicker.getValue() == null) {
            showError("Please select a delivery date");
            return false;
        }

        if (deliveryTimeComboBox.getValue() == null) {
            showError("Please select a delivery time");
            return false;
        }

        if ("DELIVERY".equals(orderTypeComboBox.getValue())) {
            if (deliveryAddressField.getText() == null || deliveryAddressField.getText().trim().isEmpty()) {
                showError("Please enter a delivery address");
                return false;
            }
            if (recipientNameField.getText() == null || recipientNameField.getText().trim().isEmpty()) {
                showError("Please enter recipient name for delivery");
                return false;
            }
            if (recipientPhoneField.getText() == null || recipientPhoneField.getText().trim().isEmpty()) {
                showError("Please enter recipient phone for delivery");
                return false;
            }
        }

        // Check if delivery date is in the future
        LocalDateTime requestedDateTime = LocalDateTime.of(
            deliveryDatePicker.getValue(),
            java.time.LocalTime.parse(deliveryTimeComboBox.getValue())
        );

        if (requestedDateTime.isBefore(LocalDateTime.now())) {
            showError("Delivery date and time must be in the future");
            return false;
        }

        return true;
    }

    private CreateOrderRequest buildOrderRequest() {
        LocalDateTime requestedDateTime = LocalDateTime.of(
            deliveryDatePicker.getValue(),
            java.time.LocalTime.parse(deliveryTimeComboBox.getValue())
        );

        CreateOrderRequest request = new CreateOrderRequest(
            currentUser.getUserId(),
            storeComboBox.getValue().getStoreId(),
            orderTypeComboBox.getValue(),
            requestedDateTime,
            "WEB"
        );

        if ("DELIVERY".equals(orderTypeComboBox.getValue())) {
            request.setDeliveryAddress(deliveryAddressField.getText().trim());
            request.setRecipientName(recipientNameField.getText().trim());
            request.setRecipientPhone(recipientPhoneField.getText().trim());
        }

        String greetingMessage = greetingCardTextArea.getText();
        if (greetingMessage != null && !greetingMessage.trim().isEmpty()) {
            request.setGreetingCardMessage(greetingMessage.trim());
        }

        String instructions = specialInstructionsTextArea.getText();
        if (instructions != null && !instructions.trim().isEmpty()) {
            request.setSpecialInstructions(instructions.trim());
        }

        return request;
    }

    @Subscribe
    public void onOrderResponse(OrderResponse response) {
        Platform.runLater(() -> {
            placeOrderButton.setDisable(false);
            placeOrderButton.setText("Place Order");

            if (response.isSuccess()) {
                showSuccess("Order placed successfully! Order ID: " + response.getOrderId());
                
                // Navigate back to catalogue after successful order
                try {
                    EventBus.getDefault().unregister(this);
                    App.setRoot("catalogue");
                } catch (IOException e) {
                    showError("Error navigating: " + e.getMessage());
                }
            } else {
                showError("Order failed: " + response.getMessage());
            }
        });
    }

    @FXML
    void goBackToCart(ActionEvent event) {
        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("cart");
        } catch (IOException e) {
            showError("Error navigating back: " + e.getMessage());
        }
    }
    

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Subscribe
    public void unsubscribe(UnsubscribeEvent event) {
        EventBus.getDefault().unregister(this);
    }
}
