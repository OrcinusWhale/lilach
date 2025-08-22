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

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginResponse;

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
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
    }

    @FXML
    void handleRegister(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            User newUser = new User(
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
            );

            App.getClient().sendToServer(newUser);
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
        return true;
    }

    @Subscribe
    public void onRegistrationResponse(LoginResponse response) {
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
