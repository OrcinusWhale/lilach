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
  private static Stage stage;
  private static SimpleClient client;
  private static String displayed;

  @Override
  public void start(Stage stage) throws IOException {
    App.stage = stage;
    scene = new Scene(loadFXML("login"), 720, 720);
    stage.setScene(scene);
    stage.show();
    
    // Initialize connection
    try {
      connect();
    } catch (IOException e) {
      System.err.println("Failed to connect to server: " + e.getMessage());
    }
  }

  public static void connect() throws IOException {
    client = SimpleClient.getClient();
    client.openConnection();
    System.out.println("Client connected to server successfully");
    // Remove the automatic "add" message - this was causing issues
    // client.sendToServer("add");
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

  public static Stage getStage() {
    return stage;
  }

  public static FXMLLoader getFxmlLoader() {
    return fxmlLoader;
  }

  public static String getDisplayed() {
    return displayed;
  }

  public static SimpleClient getClient() {
    return client;
  }

  public static void main(String[] args) {
    launch();
  }

}
