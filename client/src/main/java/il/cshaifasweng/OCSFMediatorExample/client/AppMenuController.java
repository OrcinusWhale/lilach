package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.reports.ReportsLauncher;
import javafx.fxml.FXML;

public class AppMenuController {
    @FXML
    private void openReports() {
        ReportsLauncher.open();
    }
}
