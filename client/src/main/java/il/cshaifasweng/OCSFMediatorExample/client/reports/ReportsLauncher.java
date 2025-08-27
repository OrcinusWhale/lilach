package il.cshaifasweng.OCSFMediatorExample.client.reports;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ReportsLauncher {
    private ReportsLauncher() {}

    public static void open() {
        try {
            FXMLLoader loader = new FXMLLoader(ReportsLauncher.class.getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/views/ReportsView.fxml"));
            Parent root = loader.load();
            ReportsController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Reports");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> controller.onClosed()); // unregister EventBus
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
