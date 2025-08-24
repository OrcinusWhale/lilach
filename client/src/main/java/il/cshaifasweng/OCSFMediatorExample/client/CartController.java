package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CartController {

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> itemNameColumn;

    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;

    @FXML
    private TableColumn<CartItem, Double> priceColumn;

    @FXML
    private TableColumn<CartItem, Double> subtotalColumn;

    @FXML
    private TableColumn<CartItem, Void> actionsColumn;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label discountAmountLabel;

    @FXML
    private Label finalAmountLabel;

    @FXML
    private Label itemCountLabel;

    @FXML
    private Button checkoutButton;

    @FXML
    private Button clearCartButton;

    @FXML
    private Button backButton;

    @FXML
    private Label emptyCartLabel;

    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private Cart currentCart;
    private User currentUser;

    @FXML
    void initialize() {
        System.out.println("Initializing CartController");
        EventBus.getDefault().register(this);
        setupTableColumns();
        // Delay cart loading to ensure user session is established
        Platform.runLater(() -> {
            if (UserSession.isLoggedIn()) {
                loadCart();
            } else {
                showEmptyCart();
            }
        });
    }

    private void setupTableColumns() {
        // Configure table columns with proper property binding
        System.out.println("setting up table columns");
        itemNameColumn.setCellValueFactory(cellData -> {
            CartItem cartItem = cellData.getValue();
            String displayName = "";
            if (cartItem != null) {
                // If the underlying item has ID 1, extract first substring between ':' and '|' from special requests
                try {
                    Item item = cartItem.getItem();
                    if (item != null && item.getItemId() == 1) {
                        String sr = cartItem.getSpecialRequests();
                        if (sr != null) {
                            int colonIdx = sr.indexOf(':');
                            if (colonIdx >= 0) {
                                int pipeIdx = sr.indexOf('|', colonIdx + 1);
                                if (pipeIdx > colonIdx + 1) {
                                    displayName = sr.substring(colonIdx + 1, pipeIdx).trim();
                                } else {
                                    // No following pipe, take rest of string after ':'
                                    displayName = sr.substring(colonIdx + 1).trim();
                                }
                            } else {
                                // No ':' found, fallback
                                displayName = cartItem.getItemName();
                            }
                        } else {
                            displayName = cartItem.getItemName();
                        }
                    } else {
                        displayName = cartItem.getItemName();
                    }
                } catch (Exception ex) {
                    // Fallback on any unexpected error
                    displayName = cartItem.getItemName();
                }
            }
            return new SimpleStringProperty(displayName);
        });
        
        quantityColumn.setCellValueFactory(cellData -> {
            CartItem cartItem = cellData.getValue();
            return new SimpleIntegerProperty(cartItem != null ? cartItem.getQuantity() : 0).asObject();
        });
        
        priceColumn.setCellValueFactory(cellData -> {
            CartItem cartItem = cellData.getValue();
            return new SimpleDoubleProperty(cartItem != null ? cartItem.getPrice() : 0.0).asObject();
        });
        
        subtotalColumn.setCellValueFactory(cellData -> {
            CartItem cartItem = cellData.getValue();
            return new SimpleDoubleProperty(cartItem != null ? cartItem.getSubtotal() : 0.0).asObject();
        });

        // Format price columns
        priceColumn.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        subtotalColumn.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", subtotal));
                }
            }
        });

        // Add action buttons (update quantity, remove)
        actionsColumn.setCellFactory(column -> new TableCell<CartItem, Void>() {
            private final Button decreaseBtn = new Button("-");
            private final TextField quantityField = new TextField();
            private final Button increaseBtn = new Button("+");
            private final Button removeBtn = new Button("Remove");

            {
                decreaseBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    updateQuantity(item, item.getQuantity() - 1);
                });

                increaseBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    updateQuantity(item, item.getQuantity() + 1);
                });

                removeBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    updateQuantity(item, 0);
                });

                quantityField.setPrefWidth(50);
                quantityField.setOnAction(e -> {
                    try {
                        CartItem item = getTableView().getItems().get(getIndex());
                        int newQuantity = Integer.parseInt(quantityField.getText());
                        updateQuantity(item, newQuantity);
                    } catch (NumberFormatException ex) {
                        // Reset to current quantity if invalid input
                        CartItem item = getTableView().getItems().get(getIndex());
                        quantityField.setText(String.valueOf(item.getQuantity()));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CartItem cartItem = getTableView().getItems().get(getIndex());
                    quantityField.setText(String.valueOf(cartItem.getQuantity()));
                    
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                    hbox.getChildren().addAll(decreaseBtn, quantityField, increaseBtn, removeBtn);
                    setGraphic(hbox);
                }
            }
        });

        cartTable.setItems(cartItems);
        System.out.println("Table columns set up complete");
    }

    private void loadCart() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showError("Please log in to view your cart");
            return;
        }

        try {
            System.out.println("CartController: Loading cart for user ID: " + currentUser.getUserId());
            App.getClient().sendToServer("getCart " + currentUser.getUserId());
        } catch (IOException e) {
            showError("Error loading cart: " + e.getMessage());
        }
    }

    private void showEmptyCart() {
        System.out.println("CartController: Showing empty cart");
        cartItems.clear();
        emptyCartLabel.setVisible(true);
        cartTable.setVisible(false);
        checkoutButton.setDisable(true);
        clearCartButton.setDisable(true);
        totalAmountLabel.setText("$0.00");
        discountAmountLabel.setText("$0.00");
        finalAmountLabel.setText("$0.00");
        itemCountLabel.setText("0");
    }

    private void updateQuantity(CartItem cartItem, int newQuantity) {
        if (currentUser == null) return;

        try {
            String message = "updateCart " + currentUser.getUserId() + " " + 
                           cartItem.getItem().getItemId() + " " + newQuantity;
            App.getClient().sendToServer(message);
        } catch (IOException e) {
            showError("Error updating cart: " + e.getMessage());
        }
    }

    @Subscribe
    public void onCartResponse(CartResponse response) {
        Platform.runLater(() -> {
            System.out.println("CartController: Received CartResponse - Success: " + response.isSuccess() + 
                             ", Items: " + (response.getCartItems() != null ? response.getCartItems().size() : 0));
            if (response.isSuccess()) {
                updateCartDisplay(response);
            } else {
                showError(response.getMessage());
            }
        });
    }

    private void updateCartDisplay(CartResponse response) {
        currentCart = response.getCart();
        cartItems.clear();
        
        System.out.println("CartController: Updating cart display with " + 
                         (response.getCartItems() != null ? response.getCartItems().size() : 0) + " items");
        
        if (response.getCartItems() != null && !response.getCartItems().isEmpty()) {
            cartItems.addAll(response.getCartItems());
            emptyCartLabel.setVisible(false);
            cartTable.setVisible(true);
            checkoutButton.setDisable(false);
            clearCartButton.setDisable(false);
            
            // Debug: Print each cart item
            for (CartItem item : response.getCartItems()) {
                System.out.println("CartController: Cart item - " + item.getItemName() + 
                                 ", Qty: " + item.getQuantity() + ", Price: $" + item.getPrice());
            }
        } else {
            System.out.println("CartController: No cart items found, showing empty cart");
            emptyCartLabel.setVisible(true);
            cartTable.setVisible(false);
            checkoutButton.setDisable(true);
            clearCartButton.setDisable(true);
        }

        // Update totals
        totalAmountLabel.setText(String.format("$%.2f", response.getTotalAmount()));
        discountAmountLabel.setText(String.format("$%.2f", response.getDiscountAmount()));
        finalAmountLabel.setText(String.format("$%.2f", response.getFinalAmount()));
        itemCountLabel.setText(String.valueOf(response.getTotalItemCount()));
        
        // Force table refresh
        cartTable.refresh();
    }

    @FXML
    void proceedToCheckout(ActionEvent event) {
        if (currentCart == null || cartItems.isEmpty()) {
            showError("Your cart is empty");
            return;
        }

        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("checkout");
            CheckoutController controller = App.getFxmlLoader().getController();
            controller.setCart(currentCart);
        } catch (IOException e) {
            showError("Error opening checkout: " + e.getMessage());
        }
    }

    @FXML
    void clearCart(ActionEvent event) {
        if (currentUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Are you sure you want to clear your cart?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    System.out.println("CartController: Sending clear cart request for user " + currentUser.getUserId());
                    App.getClient().sendToServer("clearCart " + currentUser.getUserId());
                } catch (IOException e) {
                    showError("Error clearing cart: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("catalogue");
        } catch (IOException e) {
            showError("Error navigating back: " + e.getMessage());
        }
    }

    // Public method to refresh cart from other controllers
    public void refreshCart() {
        if (UserSession.isLoggedIn()) {
            loadCart();
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

    @Subscribe
    public void unsubscribe(UnsubscribeEvent event) {
        EventBus.getDefault().unregister(this);
    }
}
