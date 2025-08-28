package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.reports.ReportsLauncher;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class AppMenuController {
    @FXML
    private void openReports() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("You do not have permission to view reports. Admin access required.");
            alert.showAndWait();
            return;
        }
        ReportsLauncher.open();
    }
}
