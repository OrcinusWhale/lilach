package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupResponse;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SubscriptionController implements Initializable {

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
    private TextField taxRegistrationField;

    @FXML
    private TextField customerIdField;

    @FXML
    private TextField creditCardField;

    @FXML
    private TextField customerNameField;

    @FXML
    private Button subscribeButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label infoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        
        // Set info text about the subscription
        infoLabel.setText("Annual Subscription (100₪)\n" +
                         "• Get 10% discount on purchases above 50₪\n" +
                         "• Authorized to place orders after setup\n" +
                         "• All fields are required for account setup");
    }

    @FXML
    void handleSubscribe(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            AccountSetupRequest setupRequest = new AccountSetupRequest(
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                taxRegistrationField.getText().trim(),
                customerIdField.getText().trim(),
                creditCardField.getText().trim(),
                customerNameField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
            );

            App.getClient().sendToServer(setupRequest);
            subscribeButton.setDisable(true);
            subscribeButton.setText("Setting up account...");
            hideError();
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
        // Basic user information validation
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

        // Subscription-specific validation
        if (taxRegistrationField.getText().trim().isEmpty()) {
            showError("Tax registration number is required");
            return false;
        }
        if (customerIdField.getText().trim().isEmpty()) {
            showError("Customer ID is required");
            return false;
        }
        if (creditCardField.getText().trim().isEmpty()) {
            showError("Credit card number is required");
            return false;
        }
        if (customerNameField.getText().trim().isEmpty()) {
            showError("Customer name is required");
            return false;
        }

        // Basic credit card validation (length check)
        String creditCard = creditCardField.getText().trim().replaceAll("\\s+", "");
        if (creditCard.length() < 13 || creditCard.length() > 19) {
            showError("Please enter a valid credit card number");
            return false;
        }

        // Basic email validation
        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    @Subscribe
    public void onAccountSetupResponse(AccountSetupResponse response) {
        Platform.runLater(() -> {
            subscribeButton.setDisable(false);
            subscribeButton.setText("Subscribe (100₪)");

            if (response.isSuccess()) {
                hideError();
                // Show success message and redirect to login
                try {
                    // You could show a success dialog here
                    App.setRoot("login");
                } catch (IOException e) {
                    showError("Account created successfully! Please login.");
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

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}
