package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * JavaFX App
 */
public class App extends Application {

  private static FXMLLoader fxmlLoader;
  private static Scene scene;
  private SimpleClient client;

  @Override
  public void start(Stage stage) throws IOException {
    EventBus.getDefault().register(this);
    client = SimpleClient.getClient();
    client.openConnection();
    scene = new Scene(loadFXML("catalogue"), 720, 720);
    stage.setScene(scene);
    stage.show();
    client.sendToServer("catalogue");
  }

  static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFXML(fxml));
  }

  public static Parent loadFXML(String fxml) throws IOException {
    fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    return fxmlLoader.load();
  }

  @Override
  public void stop() throws Exception {
    EventBus.getDefault().unregister(this);
    client.closeConnection();
    super.stop();
  }

  @Subscribe
  public void onWarningEvent(WarningEvent event) {
    Platform.runLater(() -> {
      Alert alert = new Alert(AlertType.WARNING,
          String.format("Message: %s\nTimestamp: %s\n",
              event.getWarning().getMessage(),
              event.getWarning().getTime().toString()));
      alert.show();
    });

  }

  public static FXMLLoader getFxmlLoader() {
    return fxmlLoader;
  }

  public static void main(String[] args) {
    launch();
  }

}
