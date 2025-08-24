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
    scene = new Scene(loadFXML("login"), 1025, 720);
    stage.setScene(scene);
    stage.show();
  }

  public static void connect() throws IOException {
    client = SimpleClient.getClient();
    client.openConnection();
    System.out.println("Client connected to server successfully");
    client.sendToServer("add");
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
    System.out.println("Application closing - cleaning up sessions");
    
    // Properly logout user session before closing
    try {
      SessionService.getInstance().cleanup();
    } catch (Exception e) {
      System.err.println("Error during session cleanup: " + e.getMessage());
    }
    
    // Send remove message and close connection
    if (client != null && client.isConnected()) {
      try {
        client.sendToServer("remove");
        client.closeConnection();
      } catch (IOException e) {
        System.err.println("Error closing client connection: " + e.getMessage());
      }
    }
    
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
