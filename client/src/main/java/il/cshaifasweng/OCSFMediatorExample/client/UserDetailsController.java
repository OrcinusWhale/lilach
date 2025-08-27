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
import il.cshaifasweng.OCSFMediatorExample.entities.UserSubscriptionSetupRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.ComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.ComplaintResponse;
import il.cshaifasweng.OCSFMediatorExample.client.reports.ReportsLauncher;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UserDetailsController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label usernameLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;

    @FXML private Label subscriptionStatusLabel;
    @FXML private Label subscriptionTypeLabel;
    @FXML private Label subscriptionDatesLabel;
    @FXML private Label subscriptionMessageLabel;

    @FXML private Button logoutButton;
    @FXML private Button browseCatalogueButton;
    @FXML private Button viewOrdersButton;
    @FXML private Button submitComplaintButton;
    @FXML private Button adminPanelButton;
    @FXML private Button employeeComplaintsButton;

    // Reports button – make sure userDetails.fxml has fx:id="viewReportsButton" and onAction="#handleViewReports"
    @FXML private Button viewReportsButton;

    @FXML private VBox subscriptionPanel;
    @FXML private VBox subscriptionBenefitsPanel;

    @FXML private Label userRoleLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label storeInfoLabel;
    @FXML private Label storeNameLabel;

    // Annual subscription setup fields
    @FXML private TextField taxRegistrationField;
    @FXML private TextField customerIdField;
    @FXML private TextField creditCardField;
    @FXML private TextField customerNameField;
    @FXML private Button setupSubscriptionButton;
    @FXML private VBox subscriptionSetupPanel;
    @FXML private Label accountValueLabel;
    @FXML private Label discountInfoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        loadUserDetails();
    }

    /* ----------------- REPORTS: button + permission ----------------- */

    @FXML
    private void handleViewReports(ActionEvent e) {
        User u = LoginController.getCurrentUser();
        if (!canSeeReports(u)) {
            showMessage("You do not have permission to view reports.", false);
            return;
        }
        ReportsLauncher.open();
    }

    private void applyReportPermission(User u) {
        boolean canSee = canSeeReports(u);
        if (viewReportsButton != null) {
            viewReportsButton.setVisible(canSee);
            viewReportsButton.setManaged(canSee);
        }
    }

    /** Permission rule: Admins (network) and employees tied to a store can see reports. Customers cannot. */
    private boolean canSeeReports(User u) {
        if (u == null) return false;
        if (u.isAdmin()) return true;                      // network manager/admin
        if (u.isEmployee() && u.getStore() != null) return true; // branch staff/manager
        return false;                                      // customers & others
    }

    /* ----------------- EXISTING SCREEN LOGIC ----------------- */

    private void loadUserDetails() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");
            usernameLabel.setText(currentUser.getUsername());
            fullNameLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());
            phoneLabel.setText(currentUser.getPhone());
            addressLabel.setText(currentUser.getAddress());

            userRoleLabel.setText("Role: " + currentUser.getUserType().toString());
            displayUserTypeAndStore(currentUser);
            configureRoleBasedAccess(currentUser);
            applyReportPermission(currentUser);
            updateSubscriptionStatus(currentUser);
        }
    }

    private void displayUserTypeAndStore(User user) {
        if (user.isBrandUser()) {
            userTypeLabel.setText("Brand User");
            userTypeLabel.setTextFill(javafx.scene.paint.Color.BLUE);
            storeInfoLabel.setVisible(false);
            storeNameLabel.setVisible(false);
        } else if (user.isStoreSpecific()) {
            userTypeLabel.setText("Store-Specific User");
            userTypeLabel.setTextFill(javafx.scene.paint.Color.PURPLE);
            if (user.getStore() != null) {
                storeInfoLabel.setVisible(true);
                storeNameLabel.setVisible(true);
                storeNameLabel.setText(user.getStore().getStoreName());
            }
        } else {
            userTypeLabel.setText("Legacy User");
            userTypeLabel.setTextFill(javafx.scene.paint.Color.GRAY);
            storeInfoLabel.setVisible(false);
            storeNameLabel.setVisible(false);
        }
    }

    private void configureRoleBasedAccess(User user) {
        if (adminPanelButton != null) {
            adminPanelButton.setVisible(user.isAdmin());
            adminPanelButton.setManaged(user.isAdmin());
        }

        if (employeeComplaintsButton != null) {
            boolean isEmployee = user.isEmployee() && !user.isAdmin();
            employeeComplaintsButton.setVisible(isEmployee);
            employeeComplaintsButton.setManaged(isEmployee);
        }

        // Show/hide complaint button – for customers, store-specific users, and brand users (hide for admins)
        if (submitComplaintButton != null) {
            boolean canSubmitComplaint =
                    (user.getUserType().toString().equals("CUSTOMER") ||
                            user.getUserType().toString().equals("STORE_SPECIFIC") ||
                            user.getUserType().toString().equals("BRAND_USER")) &&
                            !user.isAdmin();
            submitComplaintButton.setVisible(canSubmitComplaint);
            submitComplaintButton.setManaged(canSubmitComplaint);
        }

        configureSubscriptionPanelVisibility(user);
    }

    private void configureSubscriptionPanelVisibility(User user) {
        if (user.isStoreSpecific()) {
            if (subscriptionPanel != null) {
                subscriptionPanel.setVisible(false);
                subscriptionPanel.setManaged(false);
            }
        } else if (user.isBrandUser()) {
            if (subscriptionPanel != null) {
                subscriptionPanel.setVisible(true);
                subscriptionPanel.setManaged(true);
            }

            if (user.isSubscriptionActive()) {
                if (subscriptionSetupPanel != null) {
                    subscriptionSetupPanel.setVisible(false);
                    subscriptionSetupPanel.setManaged(false);
                }
                if (subscriptionBenefitsPanel != null) {
                    subscriptionBenefitsPanel.setVisible(true);
                    subscriptionBenefitsPanel.setManaged(true);
                }
            } else {
                if (subscriptionSetupPanel != null) {
                    subscriptionSetupPanel.setVisible(true);
                    subscriptionSetupPanel.setManaged(true);
                }
                if (subscriptionBenefitsPanel != null) {
                    subscriptionBenefitsPanel.setVisible(false);
                    subscriptionBenefitsPanel.setManaged(false);
                }
            }
        } else {
            if (user.isEmployee() || user.isAdmin()) {
                if (subscriptionPanel != null) {
                    subscriptionPanel.setVisible(false);
                    subscriptionPanel.setManaged(false);
                }
            } else {
                if (subscriptionPanel != null) {
                    subscriptionPanel.setVisible(true);
                    subscriptionPanel.setManaged(true);
                }
                if (subscriptionSetupPanel != null && !user.isSubscriptionActive()) {
                    subscriptionSetupPanel.setVisible(true);
                    subscriptionSetupPanel.setManaged(true);
                }
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

            if (accountValueLabel != null) {
                accountValueLabel.setText("Account Value: " + user.getAccountValue() + "₪");
                accountValueLabel.setVisible(true);
            }
            if (discountInfoLabel != null) {
                discountInfoLabel.setText("Benefits: 10% discount on purchases above 50₪");
                discountInfoLabel.setVisible(true);
            }

            if (subscriptionSetupPanel != null) {
                subscriptionSetupPanel.setVisible(false);
                subscriptionSetupPanel.setManaged(false);
            }
            if (subscriptionBenefitsPanel != null) {
                subscriptionBenefitsPanel.setVisible(true);
                subscriptionBenefitsPanel.setManaged(true);
            }
        } else {
            subscriptionStatusLabel.setText("Inactive");
            subscriptionStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
            subscriptionTypeLabel.setText("None");
            subscriptionDatesLabel.setVisible(false);

            if (accountValueLabel != null) accountValueLabel.setVisible(false);
            if (discountInfoLabel != null) discountInfoLabel.setVisible(false);

            if (subscriptionSetupPanel != null && user.isCustomer()) {
                subscriptionSetupPanel.setVisible(true);
                subscriptionSetupPanel.setManaged(true);
            }
        }
    }

    /* ----------------- Buttons / actions ----------------- */

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

        String creditCard = creditCardField.getText().trim().replaceAll("\\s+", "");
        if (creditCard.length() < 13 || creditCard.length() > 19) {
            showMessage("Please enter a valid credit card number", false);
            return false;
        }

        return true;
    }

    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Logout requested by user");
        EventBus.getDefault().unregister(this);
        SessionService.getInstance().logout();
    }

    @FXML
    void handleBrowseCatalogue(ActionEvent event) {
        try {
            App.setRoot("catalogue");
        } catch (IOException e) {
            showMessage("Error loading catalogue", false);
            e.printStackTrace();
        }
    }

    @FXML
    void handleViewOrders(ActionEvent event) {
        try {
            User currentUser = LoginController.getCurrentUser();
            if (currentUser == null) {
                showMessage("Please log in to view your orders", false);
                return;
            }
            System.out.println("View Orders requested for user: " + currentUser.getUsername());
            EventBus.getDefault().unregister(this);
            App.setRoot("orderHistory");
        } catch (IOException e) {
            showMessage("Error loading order history: " + e.getMessage(), false);
            e.printStackTrace();
        } catch (Exception e) {
            showMessage("Error loading order history: " + e.getMessage(), false);
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

    @FXML
    void handleEmployeeComplaints(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.isEmployee() || currentUser.isAdmin()) {
            showMessage("Access denied: Employee privileges required", false);
            return;
        }

        try {
            App.setRoot("employeeComplaints");
        } catch (IOException e) {
            showMessage("Error loading employee complaints panel", false);
            e.printStackTrace();
        }
    }

    @FXML
    void handleSubmitComplaint(ActionEvent event) {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            showMessage("Please log in to submit a complaint", false);
            return;
        }
        showComplaintDialog(currentUser);
    }

    private void showComplaintDialog(User currentUser) {
        Dialog<ComplaintRequest> dialog = new Dialog<>();
        dialog.setTitle("Submit Complaint");
        dialog.setHeaderText("Please describe your complaint:");

        VBox content = new VBox(10);
        content.setPrefWidth(400);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setText(currentUser.getEmail());
        emailField.setPromptText("Enter your email");

        Label orderNumberLabel = new Label("Order Number (Optional):");
        TextField orderNumberField = new TextField();
        orderNumberField.setPromptText("Enter order number if complaint is related to a specific order");

        Label descriptionLabel = new Label("Description:");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Please enter your complaint description...");
        descriptionArea.setPrefRowCount(5);
        descriptionArea.setWrapText(true);

        content.getChildren().addAll(emailLabel, emailField, orderNumberLabel, orderNumberField, descriptionLabel, descriptionArea);
        dialog.getDialogPane().setContent(content);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDefaultButton(true);

        descriptionArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == javafx.scene.input.KeyCode.ENTER && keyEvent.isControlDown()) {
                submitButton.fire();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String description = descriptionArea.getText().trim();
                if (description.isEmpty()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Validation Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Please enter a complaint description.");
                        alert.showAndWait();
                    });
                    return null;
                }
                String orderNumber = orderNumberField.getText().trim();
                String finalDescription = description;

                if (!orderNumber.isEmpty()) {
                    finalDescription = "[" + orderNumber + "]-" + description;
                }

                ComplaintRequest request = new ComplaintRequest(emailField.getText().trim(), finalDescription);
                if (!orderNumber.isEmpty()) {
                    try {
                        java.lang.reflect.Method setOrderNumberMethod = request.getClass().getMethod("setOrderNumber", String.class);
                        setOrderNumberMethod.invoke(request, orderNumber);
                    } catch (Exception ex) {
                        System.out.println("Order number feature not available in current build");
                    }
                }
                return request;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::submitComplaintToServer);
    }

    private void submitComplaintToServer(ComplaintRequest request) {
        try {
            App.getClient().sendToServer(request);
        } catch (IOException e) {
            showMessage("Failed to submit complaint: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onComplaintResponse(ComplaintResponse response) {
        System.out.println("Complaint submitted? " + response.isSuccess() + " | message: " + response.getMessage());

        Platform.runLater(() -> {
            Alert alert = new Alert(response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(response.isSuccess() ? "Complaint Submitted" : "Complaint Submission Failed");
            alert.setHeaderText(null);
            alert.setContentText(response.getMessage());
            alert.setResizable(false);
            alert.show();

            if (response.isSuccess()) {
                javafx.animation.PauseTransition delay =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                delay.setOnFinished(e -> alert.close());
                delay.play();
            }
        });
    }

    @Subscribe
    public void onAccountSetupResponse(AccountSetupResponse response) {
        Platform.runLater(() -> {
            setupSubscriptionButton.setDisable(false);
            setupSubscriptionButton.setText("Setup Annual Subscription (100₪)");

            if (response.isSuccess()) {
                showMessage("Annual subscription activated successfully! You now have 10% discount on purchases above 50₪.", true);

                taxRegistrationField.clear();
                customerIdField.clear();
                creditCardField.clear();
                customerNameField.clear();

                User currentUser = LoginController.getCurrentUser();
                if (currentUser != null && response.getUser() != null) {
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
                    applyReportPermission(currentUser);
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
