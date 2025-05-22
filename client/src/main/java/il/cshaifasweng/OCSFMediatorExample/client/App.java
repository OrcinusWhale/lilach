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
  private static SimpleClient client;
  private static String displayed;

  @Override
  public void start(Stage stage) throws IOException {
    EventBus.getDefault().register(this);
    scene = new Scene(loadFXML("connect"), 720, 720);
    stage.setScene(scene);
    stage.show();
  }

  public static void connect() throws IOException {
    client = SimpleClient.getClient();
    client.openConnection();
    setRoot("catalogue");
    client.sendToServer("add");
    client.sendToServer("catalogue");
  }

  static void setRoot(String fxml) throws IOException {
    displayed = fxml;
    scene.setRoot(loadFXML(fxml));
  }

  public static Parent loadFXML(String fxml) throws IOException {
    fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    return fxmlLoader.load();
  }

  @Override
  public void stop() throws Exception {
    EventBus.getDefault().unregister(this);
    client.sendToServer("remove");
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

  public static String getDisplayed() {
    return displayed;
  }

  public static void main(String[] args) {
    launch();
  }

}
