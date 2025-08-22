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
import il.cshaifasweng.OCSFMediatorExample.entities.SubscriptionRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.SubscriptionResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.UserSubscriptionSetupRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupResponse;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UserDetailsController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private Label subscriptionStatusLabel;

    @FXML
    private Label subscriptionTypeLabel;

    @FXML
    private Label subscriptionDatesLabel;

    @FXML
    private Label subscriptionMessageLabel;


    @FXML
    private Button logoutButton;

    @FXML
    private Button browseCatalogueButton;

    @FXML
    private VBox subscriptionBenefitsPanel;

    @FXML
    private Button adminPanelButton;

    @FXML
    private Label userRoleLabel;

    // Annual subscription setup fields
    @FXML
    private TextField taxRegistrationField;

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField creditCardField;

    @FXML
    private TextField customerNameField;

    @FXML
    private Button setupSubscriptionButton;

    @FXML
    private VBox subscriptionSetupPanel;

    @FXML
    private Label accountValueLabel;

    @FXML
    private Label discountInfoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        loadUserDetails();
    }

    private void loadUserDetails() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");
            usernameLabel.setText(currentUser.getUsername());
            fullNameLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());
            phoneLabel.setText(currentUser.getPhone());
            addressLabel.setText(currentUser.getAddress());

            // Display user role
            userRoleLabel.setText("Role: " + currentUser.getUserType().toString());
            
            // Configure role-based access
            configureRoleBasedAccess(currentUser);
            
            updateSubscriptionStatus(currentUser);
        }
    }

    private void configureRoleBasedAccess(User user) {
        // Show/hide admin panel button based on user role
        if (adminPanelButton != null) {
            adminPanelButton.setVisible(user.isAdmin());
            adminPanelButton.setManaged(user.isAdmin());
        }
        
        // Configure subscription panel visibility based on user type
        if (user.isEmployee() || user.isAdmin()) {
            // Employees and admins don't need subscription setup
            if (subscriptionSetupPanel != null) {
                subscriptionSetupPanel.setVisible(false);
                subscriptionSetupPanel.setManaged(false);
            }
        } else {
            // Only customers can setup subscriptions
            if (subscriptionSetupPanel != null && !user.isSubscriptionActive()) {
                subscriptionSetupPanel.setVisible(true);
                subscriptionSetupPanel.setManaged(true);
            }
        }
    }

    private void updateSubscriptionStatus(User user) {
        if (user.isSubscriptionActive()) {
            subscriptionStatusLabel.setText("Active");
            subscriptionStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            subscriptionTypeLabel.setText(user.getSubscriptionType().toString());
            
            if (user.getSubscriptionStartDate() != null && user.getSubscriptionEndDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                subscriptionDatesLabel.setText("Valid from " + 
                    user.getSubscriptionStartDate().format(formatter) + 
                    " to " + user.getSubscriptionEndDate().format(formatter));
                subscriptionDatesLabel.setVisible(true);
            }
            
            // Show subscription benefits for active users
            if (accountValueLabel != null) {
                accountValueLabel.setText("Account Value: " + user.getAccountValue() + "₪");
                accountValueLabel.setVisible(true);
            }
            if (discountInfoLabel != null) {
                discountInfoLabel.setText("Benefits: 10% discount on purchases above 50₪");
                discountInfoLabel.setVisible(true);
            }
            
            // Hide subscription setup panels for active users
            if (subscriptionSetupPanel != null) {
                subscriptionSetupPanel.setVisible(false);
                subscriptionSetupPanel.setManaged(false);
            }
            
            // Show subscription benefits panel for active users
            if (subscriptionBenefitsPanel != null) {
                subscriptionBenefitsPanel.setVisible(true);
                subscriptionBenefitsPanel.setManaged(true);
            }
        } else {
            subscriptionStatusLabel.setText("Inactive");
            subscriptionStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
            subscriptionTypeLabel.setText("None");
            subscriptionDatesLabel.setVisible(false);
            
            // Hide account value and discount info for inactive users
            if (accountValueLabel != null) accountValueLabel.setVisible(false);
            if (discountInfoLabel != null) discountInfoLabel.setVisible(false);
            
            // Show annual subscription setup panel instead of old subscription request
            if (subscriptionSetupPanel != null && user.isCustomer()) {
                subscriptionSetupPanel.setVisible(true);
                subscriptionSetupPanel.setManaged(true);
            }
        }
    }


    @FXML
    void handleSetupSubscription(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            showMessage("Error: User not logged in", false);
            return;
        }

        if (!validateSubscriptionSetupFields()) {
            return;
        }

        try {
            UserSubscriptionSetupRequest request = new UserSubscriptionSetupRequest(
                currentUser.getUserId(),
                taxRegistrationField.getText().trim(),
                customerIdField.getText().trim(),
                creditCardField.getText().trim(),
                customerNameField.getText().trim()
            );

            App.getClient().sendToServer(request);
            setupSubscriptionButton.setDisable(true);
            setupSubscriptionButton.setText("Setting up...");
        } catch (IOException e) {
            showMessage("Connection error. Please try again.", false);
            e.printStackTrace();
        }
    }

    private boolean validateSubscriptionSetupFields() {
        // Debug logging
        System.out.println("Validating subscription fields...");
        System.out.println("Tax Registration: " + (taxRegistrationField != null ? "'" + taxRegistrationField.getText() + "'" : "NULL"));
        System.out.println("Customer ID: " + (customerIdField != null ? "'" + customerIdField.getText() + "'" : "NULL"));
        System.out.println("Credit Card: " + (creditCardField != null ? "'" + creditCardField.getText() + "'" : "NULL"));
        System.out.println("Customer Name: " + (customerNameField != null ? "'" + customerNameField.getText() + "'" : "NULL"));
        
        if (taxRegistrationField == null || taxRegistrationField.getText().trim().isEmpty()) {
            showMessage("Tax registration number is required", false);
            return false;
        }
        if (customerIdField == null || customerIdField.getText().trim().isEmpty()) {
            showMessage("Customer ID is required", false);
            return false;
        }
        if (creditCardField == null || creditCardField.getText().trim().isEmpty()) {
            showMessage("Credit card number is required", false);
            return false;
        }
        if (customerNameField == null || customerNameField.getText().trim().isEmpty()) {
            showMessage("Customer name is required", false);
            return false;
        }

        // Basic credit card validation
        String creditCard = creditCardField.getText().trim().replaceAll("\\s+", "");
        if (creditCard.length() < 13 || creditCard.length() > 19) {
            showMessage("Please enter a valid credit card number", false);
            return false;
        }

        return true;
    }

    @FXML
    void handleLogout(ActionEvent event) {
        LoginController.setCurrentUser(null);
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBrowseCatalogue(ActionEvent event) {
        try {
            // Don't send catalogue request here - let CatalogueController handle it
            App.setRoot("catalogue");
        } catch (IOException e) {
            showMessage("Error loading catalogue", false);
            e.printStackTrace();
        }
    }

    @FXML
    void handleAdminPanel(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            showMessage("Access denied: Admin privileges required", false);
            return;
        }
        
        try {
            App.setRoot("admin");
        } catch (IOException e) {
            showMessage("Error loading admin panel", false);
            e.printStackTrace();
        }
    }


    @Subscribe
    public void onAccountSetupResponse(AccountSetupResponse response) {
        Platform.runLater(() -> {
            setupSubscriptionButton.setDisable(false);
            setupSubscriptionButton.setText("Setup Annual Subscription (100₪)");

            if (response.isSuccess()) {
                showMessage("Annual subscription activated successfully! You now have 10% discount on purchases above 50₪.", true);
                
                // Clear the form fields
                taxRegistrationField.clear();
                customerIdField.clear();
                creditCardField.clear();
                customerNameField.clear();
                
                // Update current user's subscription status
                User currentUser = LoginController.getCurrentUser();
                if (currentUser != null && response.getUser() != null) {
                    // Update the current user with the new subscription data
                    User updatedUser = response.getUser();
                    currentUser.setSubscriptionType(updatedUser.getSubscriptionType());
                    currentUser.setSubscriptionActive(updatedUser.isSubscriptionActive());
                    currentUser.setSubscriptionStartDate(updatedUser.getSubscriptionStartDate());
                    currentUser.setSubscriptionEndDate(updatedUser.getSubscriptionEndDate());
                    currentUser.setAccountValue(updatedUser.getAccountValue());
                    currentUser.setTaxRegistrationNumber(updatedUser.getTaxRegistrationNumber());
                    currentUser.setCustomerId(updatedUser.getCustomerId());
                    currentUser.setCustomerName(updatedUser.getCustomerName());
                    
                    updateSubscriptionStatus(currentUser);
                }
            } else {
                showMessage(response.getMessage(), false);
            }
        });
    }

    private void showMessage(String message, boolean isSuccess) {
        subscriptionMessageLabel.setText(message);
        subscriptionMessageLabel.setTextFill(
            isSuccess ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED
        );
        subscriptionMessageLabel.setVisible(true);
    }
}
