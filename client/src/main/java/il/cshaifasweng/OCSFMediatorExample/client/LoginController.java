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

import il.cshaifasweng.OCSFMediatorExample.entities.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField ipField; // New field for server IP

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String ipAddress = ipField.getText().trim();
        SimpleClient.setHostIp(ipAddress);
        try {
            App.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            App.getClient().sendToServer(loginRequest);
            loginButton.setDisable(true);
            loginButton.setText("Logging in...");
        } catch (IOException e) {
            showError("Connection error. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            App.setRoot("register");
        } catch (IOException e) {
            showError("Error opening registration form");
            e.printStackTrace();
        }
    }


    @Subscribe
    public void onLoginResponse(LoginResponse response) {
        Platform.runLater(() -> {
            loginButton.setDisable(false);
            loginButton.setText("Login");

            if (response.isSuccess()) {
                // TODO: Session management temporarily disabled - just set user without session ID
                UserSession.setCurrentUser(response.getUser());
                System.out.println("Login successful for user: " + response.getUser().getUsername());
                
                hideError();
                try {
                    EventBus.getDefault().unregister(this);
                    App.setRoot("userDetails");
                } catch (IOException e) {
                    showError("Error loading user details");
                    e.printStackTrace();
                }
            } else {
                // Handle session conflicts by checking message prefix
                if (response.getMessage().startsWith("SESSION_CONFLICT:")) {
                    String cleanMessage = response.getMessage().substring("SESSION_CONFLICT:".length()).trim();
                    showSessionConflictError(cleanMessage);
                } else {
                    showError(response.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSessionConflictError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorLabel.setStyle("-fx-text-fill: orange;");
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setStyle("-fx-text-fill: red;"); // Reset to default error color
        errorLabel.setVisible(false);
    }

    public static User getCurrentUser() {
        return UserSession.getCurrentUser();
    }

    public static void setCurrentUser(User user) {
        UserSession.setCurrentUser(user);
    }
}
