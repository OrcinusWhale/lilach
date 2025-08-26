package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.client.LoginController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeeComplaintsController implements Initializable {

    @FXML
    private TableView<Complaint> complaintsTable;

    @FXML
    private TableColumn<Complaint, Integer> idColumn;

    @FXML
    private TableColumn<Complaint, String> customerEmailColumn;

    @FXML
    private TableColumn<Complaint, String> statusColumn;

    @FXML
    private TableColumn<Complaint, String> assignedEmployeeColumn;

    @FXML
    private TableColumn<Complaint, String> deadlineColumn;

    @FXML
    private TableColumn<Complaint, String> createdAtColumn;

    @FXML
    private TableColumn<Complaint, String> descriptionColumn;

    @FXML
    private Button processComplaintButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    @FXML
    private TextField compensationAmountField;

    @FXML
    private TextField compensationCurrencyField;

    @FXML
    private TextField compensationNoteField;

    @FXML
    private Label statusLabel;

    private ObservableList<Complaint> complaints = FXCollections.observableArrayList();
    private String currentEmployeeUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        
        // Get current employee username
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            currentEmployeeUsername = currentUser.getUsername();
        }
        
        setupTableColumns();
        setupTableSelection();
        loadComplaints();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Use custom cell value factory for assignedEmployee to avoid reflection issues
        assignedEmployeeColumn.setCellValueFactory(cellData -> {
            try {
                // Try to get assignedEmployee using reflection as fallback
                java.lang.reflect.Method method = cellData.getValue().getClass().getMethod("getAssignedEmployee");
                Object result = method.invoke(cellData.getValue());
                return new SimpleStringProperty(result != null ? result.toString() : "");
            } catch (Exception e) {
                return new SimpleStringProperty("");
            }
        });
        
        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                String formattedDate = cellData.getValue().getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new SimpleStringProperty(formattedDate);
            }
            return new SimpleStringProperty("");
        });
        
        deadlineColumn.setCellValueFactory(cellData -> {
            try {
                java.lang.reflect.Method method = cellData.getValue().getClass().getMethod("getDeadline");
                Object result = method.invoke(cellData.getValue());
                if (result != null) {
                    java.time.Instant deadline = (java.time.Instant) result;
                    String formattedDate = deadline
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return new SimpleStringProperty(formattedDate);
                }
            } catch (Exception e) {
                // Fallback: calculate 24h from creation
                if (cellData.getValue().getCreatedAt() != null) {
                    String formattedDate = cellData.getValue().getCreatedAt()
                        .plus(24, java.time.temporal.ChronoUnit.HOURS)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return new SimpleStringProperty(formattedDate);
                }
            }
            return new SimpleStringProperty("");
        });
        
        descriptionColumn.setCellValueFactory(cellData -> {
            String description = cellData.getValue().getDescription();
            if (description != null && description.length() > 50) {
                return new SimpleStringProperty(description.substring(0, 50) + "...");
            }
            return new SimpleStringProperty(description != null ? description : "");
        });
        
        complaintsTable.setItems(complaints);
    }

    private void setupTableSelection() {
        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                System.out.println("Selected complaint: " + newSelection.getId() + " - " + newSelection.getCustomerEmail());
            }
        });
    }

    private void loadComplaints() {
        try {
            statusLabel.setText("Loading complaints...");
            App.getClient().sendToServer("getAllComplaints");
        } catch (IOException e) {
            showAlert("Error", "Failed to load complaints: " + e.getMessage(), Alert.AlertType.ERROR);
            statusLabel.setText("Error loading complaints");
        }
    }

    @FXML
    void handleApproveCompensation(ActionEvent event) {
        Complaint selectedComplaint = complaintsTable.getSelectionModel().getSelectedItem();
        if (selectedComplaint == null) {
            showAlert("No Selection", "Please select a complaint to approve compensation.", Alert.AlertType.WARNING);
            return;
        }

        String compensationAmount = compensationAmountField.getText().trim();
        String currency = compensationCurrencyField.getText().trim();
        String note = compensationNoteField.getText().trim();

        if (compensationAmount.isEmpty()) {
            showAlert("Missing Information", "Please enter a compensation amount.", Alert.AlertType.WARNING);
            return;
        }

        if (currency.isEmpty()) {
            currency = "USD"; // Default currency
        }

        try {
            // Show email notification message
            String emailMessage = String.format(
                "Email sent to customer %s:\n\n" +
                "Dear Customer,\n\n" +
                "Your complaint has been processed and resolved.\n" +
                "Compensation: %s %s\n" +
                "Note: %s\n\n" +
                "Thank you for your patience.\n" +
                "Lilach Customer Service Team",
                selectedComplaint.getCustomerEmail(),
                compensationAmount,
                currency,
                note.isEmpty() ? "Complaint resolved satisfactorily" : note
            );
            
            showAlert("Email Sent", emailMessage, Alert.AlertType.INFORMATION);
            
            // Remove the complaint from the table (simulating deletion)
            complaints.remove(selectedComplaint);
            statusLabel.setText("Compensation approved and email sent to customer.");
            
            // Clear the form
            compensationAmountField.clear();
            compensationNoteField.clear();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to approve compensation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        try {
            statusLabel.setText("Refreshing complaints...");
            App.getClient().sendToServer("getAllComplaints");
        } catch (IOException e) {
            showAlert("Error", "Failed to refresh complaints: " + e.getMessage(), Alert.AlertType.ERROR);
            statusLabel.setText("Error refreshing complaints");
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            EventBus.getDefault().unregister(this);
            App.setRoot("userDetails");
        } catch (IOException e) {
            showAlert("Error", "Failed to return to main screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Subscribe
    public void onComplaintActionResponse(ComplaintActionResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                showAlert("Success", response.getMessage(), Alert.AlertType.INFORMATION);
                loadComplaints(); // Refresh the list
                clearCompensationForm();
            } else {
                showAlert("Error", response.getMessage(), Alert.AlertType.ERROR);
            }
            statusLabel.setText("Ready");
        });
    }

    @Subscribe
    public void onComplaintsList(List complaintsList) {
        Platform.runLater(() -> {
            System.out.println("EmployeeComplaintsController received complaints list: " + complaintsList.size() + " items");
            complaints.clear();
            complaints.addAll(complaintsList);
            statusLabel.setText("Loaded " + complaintsList.size() + " complaints");
        });
    }

    @Subscribe
    public void onGenericMessage(Object message) {
        System.out.println("EmployeeComplaintsController received message: " + message.getClass().getSimpleName());
        if (message instanceof List) {
            List<?> list = (List<?>) message;
            if (!list.isEmpty() && list.get(0) instanceof Complaint) {
                System.out.println("Found complaint list with " + list.size() + " items");
                onComplaintsList((List) list);
            }
        }
    }

    private void clearCompensationForm() {
        compensationAmountField.clear();
        compensationCurrencyField.setText("ILS");
        compensationNoteField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
