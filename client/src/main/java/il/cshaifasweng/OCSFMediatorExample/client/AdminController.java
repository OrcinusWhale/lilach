package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.EmployeeManagementRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.EmployeeManagementResponse;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private TableView<Employee> employeeTable;
    
    @FXML
    private TableColumn<Employee, String> employeeNumberColumn;
    
    @FXML
    private TableColumn<Employee, String> nameColumn;
    
    @FXML
    private TableColumn<Employee, String> departmentColumn;
    
    @FXML
    private TableColumn<Employee, String> positionColumn;
    
    @FXML
    private TableColumn<Employee, String> statusColumn;
    
    @FXML
    private TextField employeeNumberField;
    
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
    private TextField departmentField;
    
    @FXML
    private TextField positionField;
    
    @FXML
    private TextField salaryField;
    
    @FXML
    private ComboBox<Employee.EmployeeStatus> statusComboBox;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button addEmployeeButton;
    
    @FXML
    private Button updateEmployeeButton;
    
    @FXML
    private Button deleteEmployeeButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label statusLabel;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private Employee selectedEmployee = null;
    private User currentAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getDefault().register(this);
        
        // Check if current user is admin
        currentAdmin = LoginController.getCurrentUser();
        if (currentAdmin == null || !currentAdmin.isAdmin()) {
            showError("Access denied: Admin privileges required");
            try {
                App.setRoot("login");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        setupTableColumns();
        setupStatusComboBox();
        setupTableSelection();
        loadEmployees();
    }
    
    private void setupTableColumns() {
        employeeNumberColumn.setCellValueFactory(new PropertyValueFactory<>("employeeNumber"));
        nameColumn.setCellValueFactory(cellData -> {
            Employee emp = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                emp.getUser().getFirstName() + " " + emp.getUser().getLastName()
            );
        });
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        employeeTable.setItems(employeeList);
    }
    
    private void setupStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList(Employee.EmployeeStatus.values()));
        statusComboBox.setValue(Employee.EmployeeStatus.ACTIVE);
    }
    
    private void setupTableSelection() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEmployee = newSelection;
                populateFields(newSelection);
                updateEmployeeButton.setDisable(false);
                deleteEmployeeButton.setDisable(false);
            } else {
                selectedEmployee = null;
                clearFields();
                updateEmployeeButton.setDisable(true);
                deleteEmployeeButton.setDisable(true);
            }
        });
    }
    
    private void populateFields(Employee employee) {
        User user = employee.getUser();
        
        employeeNumberField.setText(employee.getEmployeeNumber());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        addressField.setText(user.getAddress());
        departmentField.setText(employee.getDepartment());
        positionField.setText(employee.getPosition());
        salaryField.setText(employee.getSalary() != null ? employee.getSalary().toString() : "");
        statusComboBox.setValue(employee.getStatus());
        usernameField.setText(user.getUsername());
        passwordField.setText(""); // Don't show password
    }
    
    private void clearFields() {
        employeeNumberField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        departmentField.clear();
        positionField.clear();
        salaryField.clear();
        statusComboBox.setValue(Employee.EmployeeStatus.ACTIVE);
        usernameField.clear();
        passwordField.clear();
    }
    
    @FXML
    void handleAddEmployee(ActionEvent event) {
        if (!validateFields()) {
            return;
        }
        
        try {
            // Create new user
            User newUser = new User(
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
            );
            newUser.setUserType(User.UserType.EMPLOYEE);
            
            // Create new employee
            Employee newEmployee = new Employee(
                newUser,
                employeeNumberField.getText().trim(),
                departmentField.getText().trim(),
                positionField.getText().trim()
            );
            
            if (!salaryField.getText().trim().isEmpty()) {
                newEmployee.setSalary(Double.parseDouble(salaryField.getText().trim()));
            }
            newEmployee.setStatus(statusComboBox.getValue());
            
            // Send request to server
            EmployeeManagementRequest request = new EmployeeManagementRequest(
                EmployeeManagementRequest.RequestType.CREATE_EMPLOYEE,
                currentAdmin,
                newEmployee
            );
            
            App.getClient().sendToServer(request);
            setButtonsEnabled(false);
            showStatus("Creating employee...");
            
        } catch (NumberFormatException e) {
            showError("Invalid salary format");
        } catch (IOException e) {
            showError("Connection error: " + e.getMessage());
        }
    }
    
    @FXML
    void handleUpdateEmployee(ActionEvent event) {
        if (selectedEmployee == null || !validateFields()) {
            return;
        }
        
        try {
            // Update user information
            User updatedUser = selectedEmployee.getUser();
            updatedUser.setFirstName(firstNameField.getText().trim());
            updatedUser.setLastName(lastNameField.getText().trim());
            updatedUser.setEmail(emailField.getText().trim());
            updatedUser.setPhone(phoneField.getText().trim());
            updatedUser.setAddress(addressField.getText().trim());
            
            // Update employee information
            selectedEmployee.setDepartment(departmentField.getText().trim());
            selectedEmployee.setPosition(positionField.getText().trim());
            selectedEmployee.setStatus(statusComboBox.getValue());
            
            if (!salaryField.getText().trim().isEmpty()) {
                selectedEmployee.setSalary(Double.parseDouble(salaryField.getText().trim()));
            }
            
            // Send request to server
            EmployeeManagementRequest request = new EmployeeManagementRequest(
                EmployeeManagementRequest.RequestType.UPDATE_EMPLOYEE,
                currentAdmin,
                selectedEmployee
            );
            
            App.getClient().sendToServer(request);
            setButtonsEnabled(false);
            showStatus("Updating employee...");
            
        } catch (NumberFormatException e) {
            showError("Invalid salary format");
        } catch (IOException e) {
            showError("Connection error: " + e.getMessage());
        }
    }
    
    @FXML
    void handleDeleteEmployee(ActionEvent event) {
        if (selectedEmployee == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Termination");
        alert.setHeaderText("Terminate Employee");
        alert.setContentText("Are you sure you want to terminate this employee? This will preserve all operational records but revoke access.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                EmployeeManagementRequest request = new EmployeeManagementRequest(
                    EmployeeManagementRequest.RequestType.DELETE_EMPLOYEE,
                    currentAdmin,
                    selectedEmployee.getEmployeeId()
                );
                
                App.getClient().sendToServer(request);
                setButtonsEnabled(false);
                showStatus("Terminating employee...");
                
            } catch (IOException e) {
                showError("Connection error: " + e.getMessage());
            }
        }
    }
    
    @FXML
    void handleRefresh(ActionEvent event) {
        loadEmployees();
    }
    
    @FXML
    void handleBack(ActionEvent event) {
        try {
            App.setRoot("userDetails");
        } catch (IOException e) {
            showError("Error navigating back: " + e.getMessage());
        }
    }
    
    private void loadEmployees() {
        try {
            EmployeeManagementRequest request = new EmployeeManagementRequest(
                EmployeeManagementRequest.RequestType.GET_ALL_EMPLOYEES,
                currentAdmin
            );
            
            App.getClient().sendToServer(request);
            showStatus("Loading employees...");
            
        } catch (IOException e) {
            showError("Connection error: " + e.getMessage());
        }
    }
    
    @Subscribe
    public void onEmployeeManagementResponse(EmployeeManagementResponse response) {
        Platform.runLater(() -> {
            setButtonsEnabled(true);
            
            if (response.isSuccess()) {
                switch (response.getMessage()) {
                    case "Employees retrieved successfully":
                        employeeList.clear();
                        if (response.getEmployees() != null) {
                            employeeList.addAll(response.getEmployees());
                        }
                        showStatus("Employees loaded successfully");
                        break;
                        
                    case "Employee created successfully":
                        clearFields();
                        loadEmployees(); // Refresh the list
                        showStatus("Employee created successfully");
                        break;
                        
                    case "Employee updated successfully":
                        loadEmployees(); // Refresh the list
                        showStatus("Employee updated successfully");
                        break;
                        
                    case "Employee terminated successfully":
                        clearFields();
                        loadEmployees(); // Refresh the list
                        showStatus("Employee terminated successfully");
                        break;
                        
                    default:
                        showStatus(response.getMessage());
                }
            } else {
                showError(response.getMessage());
            }
        });
    }
    
    private boolean validateFields() {
        if (usernameField.getText().trim().isEmpty() ||
            firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            employeeNumberField.getText().trim().isEmpty() ||
            departmentField.getText().trim().isEmpty() ||
            positionField.getText().trim().isEmpty()) {
            
            showError("Please fill in all required fields");
            return false;
        }
        
        if (selectedEmployee == null && passwordField.getText().trim().isEmpty()) {
            showError("Password is required for new employees");
            return false;
        }
        
        return true;
    }
    
    private void setButtonsEnabled(boolean enabled) {
        addEmployeeButton.setDisable(!enabled);
        updateEmployeeButton.setDisable(!enabled || selectedEmployee == null);
        deleteEmployeeButton.setDisable(!enabled || selectedEmployee == null);
        refreshButton.setDisable(!enabled);
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: black;");
    }
}
