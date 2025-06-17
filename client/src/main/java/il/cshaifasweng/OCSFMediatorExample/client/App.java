package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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
    client.sendToServer("remove");
    client.closeConnection();
    super.stop();
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
