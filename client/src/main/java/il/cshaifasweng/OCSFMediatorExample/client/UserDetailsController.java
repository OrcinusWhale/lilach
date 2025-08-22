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
    private ComboBox<User.SubscriptionType> subscriptionTypeCombo;

    @FXML
    private TextArea justificationArea;

    @FXML
    private Button requestSubscriptionButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button browseCatalogueButton;

    @FXML
    private VBox subscriptionRequestPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        initializeSubscriptionCombo();
        loadUserDetails();
    }

    private void initializeSubscriptionCombo() {
        subscriptionTypeCombo.getItems().addAll(
            User.SubscriptionType.BASIC,
            User.SubscriptionType.PREMIUM,
            User.SubscriptionType.VIP
        );
        subscriptionTypeCombo.setValue(User.SubscriptionType.BASIC);
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

            updateSubscriptionStatus(currentUser);
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
            
            subscriptionRequestPanel.setVisible(false);
        } else {
            subscriptionStatusLabel.setText("Inactive");
            subscriptionStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
            subscriptionTypeLabel.setText("None");
            subscriptionDatesLabel.setVisible(false);
            subscriptionRequestPanel.setVisible(user.canRequestSubscription());
        }
    }

    @FXML
    void handleRequestSubscription(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            showMessage("Error: User not logged in", false);
            return;
        }

        String justification = justificationArea.getText().trim();
        if (justification.isEmpty()) {
            showMessage("Please provide justification for your subscription request", false);
            return;
        }

        try {
            SubscriptionRequest request = new SubscriptionRequest(
                currentUser.getUserId(),
                subscriptionTypeCombo.getValue(),
                justification
            );

            App.getClient().sendToServer(request);
            requestSubscriptionButton.setDisable(true);
            requestSubscriptionButton.setText("Requesting...");
        } catch (IOException e) {
            showMessage("Connection error. Please try again.", false);
            e.printStackTrace();
        }
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

    @Subscribe
    public void onSubscriptionResponse(SubscriptionResponse response) {
        Platform.runLater(() -> {
            requestSubscriptionButton.setDisable(false);
            requestSubscriptionButton.setText("Request Subscription");

            if (response.isSuccess()) {
                showMessage("Subscription request submitted successfully! You will be notified once it's processed.", true);
                justificationArea.clear();
                
                // Update current user's subscription status if approved immediately
                User currentUser = LoginController.getCurrentUser();
                if (currentUser != null && response.getApprovedSubscriptionType() != null) {
                    currentUser.setSubscriptionType(response.getApprovedSubscriptionType());
                    currentUser.setSubscriptionActive(true);
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
