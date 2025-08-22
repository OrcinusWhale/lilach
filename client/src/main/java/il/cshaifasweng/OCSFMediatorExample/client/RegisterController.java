package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.Store;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListResponse;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private RadioButton brandUserRadio;

    @FXML
    private RadioButton storeSpecificRadio;

    @FXML
    private ToggleGroup userTypeGroup;

    @FXML
    private Label storeLabel;

    @FXML
    private ComboBox<Store> storeComboBox;

    @FXML
    private VBox subscriptionPanel;

    @FXML
    private TextField taxRegField;

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField creditCardField;

    @FXML
    private TextField customerNameField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        
        // Set up user type radio button listeners
        brandUserRadio.setOnAction(e -> updateUIBasedOnUserType());
        storeSpecificRadio.setOnAction(e -> updateUIBasedOnUserType());
        
        // Load stores from server
        loadStoresFromServer();
        
        // Initialize UI state
        updateUIBasedOnUserType();
    }
    
    private void updateUIBasedOnUserType() {
        boolean isStoreSpecific = storeSpecificRadio.isSelected();
        
        // Show/hide store selection
        storeLabel.setVisible(isStoreSpecific);
        storeComboBox.setVisible(isStoreSpecific);
        
        // Show/hide subscription panel
        subscriptionPanel.setVisible(!isStoreSpecific);
    }
    
    private void loadStoresFromServer() {
        try {
            SimpleClient.getClient().sendToServer(new StoreListRequest());
        } catch (IOException e) {
            Platform.runLater(() -> {
                errorLabel.setText("Error loading stores: " + e.getMessage());
                errorLabel.setVisible(true);
            });
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            // Determine user type
            User.UserType userType = brandUserRadio.isSelected() ? User.UserType.BRAND_USER : User.UserType.STORE_SPECIFIC;
            
            // Get store ID for store-specific users
            Integer storeId = null;
            if (storeSpecificRadio.isSelected()) {
                Store selectedStore = storeComboBox.getSelectionModel().getSelectedItem();
                if (selectedStore != null) {
                    storeId = selectedStore.getStoreId();
                }
            }
            
            // Create AccountSetupRequest
            AccountSetupRequest setupRequest = new AccountSetupRequest();
            setupRequest.setUsername(usernameField.getText().trim());
            setupRequest.setPassword(passwordField.getText().trim());
            setupRequest.setFirstName(firstNameField.getText().trim());
            setupRequest.setLastName(lastNameField.getText().trim());
            setupRequest.setEmail(emailField.getText().trim());
            setupRequest.setPhone(phoneField.getText().trim());
            setupRequest.setAddress(addressField.getText().trim());
            setupRequest.setUserType(userType);
            setupRequest.setStoreId(storeId);
            
            // Add subscription details for brand users
            if (brandUserRadio.isSelected()) {
                setupRequest.setTaxRegistrationNumber(taxRegField.getText().trim());
                setupRequest.setCustomerId(customerIdField.getText().trim());
                setupRequest.setCreditCard(creditCardField.getText().trim());
                setupRequest.setCustomerName(customerNameField.getText().trim());
            }

            SimpleClient.getClient().sendToServer(setupRequest);
            registerButton.setDisable(true);
            registerButton.setText("Registering...");
        } catch (IOException e) {
            showError("Connection error. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            showError("Error returning to login");
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        // Basic field validation
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            return false;
        }
        if (passwordField.getText().trim().isEmpty()) {
            showError("Password is required");
            return false;
        }
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required");
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required");
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showError("Phone is required");
            return false;
        }
        if (addressField.getText().trim().isEmpty()) {
            showError("Address is required");
            return false;
        }
        
        // User type specific validation
        if (storeSpecificRadio.isSelected()) {
            if (storeComboBox.getSelectionModel().getSelectedItem() == null) {
                showError("Please select a store for store-specific users");
                return false;
            }
        } else if (brandUserRadio.isSelected()) {
            // Validate subscription fields for brand users
            if (taxRegField.getText().trim().isEmpty()) {
                showError("Tax registration number is required for brand users");
                return false;
            }
            if (customerIdField.getText().trim().isEmpty()) {
                showError("Customer ID is required for brand users");
                return false;
            }
            if (creditCardField.getText().trim().isEmpty()) {
                showError("Credit card number is required for brand users");
                return false;
            }
            if (customerNameField.getText().trim().isEmpty()) {
                showError("Customer name is required for brand users");
                return false;
            }
        }
        
        return true;
    }

    @Subscribe
    public void onStoreListResponse(StoreListResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                storeComboBox.getItems().clear();
                storeComboBox.getItems().addAll(response.getStores());
            } else {
                showError("Failed to load stores: " + response.getMessage());
            }
        });
    }

    @Subscribe
    public void onRegistrationResponse(AccountSetupResponse response) {
        Platform.runLater(() -> {
            registerButton.setDisable(false);
            registerButton.setText("Register");

            if (response.isSuccess()) {
                hideError();
                try {
                    App.setRoot("login");
                } catch (IOException e) {
                    showError("Error returning to login");
                    e.printStackTrace();
                }
            } else {
                showError(response.getMessage());
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}
