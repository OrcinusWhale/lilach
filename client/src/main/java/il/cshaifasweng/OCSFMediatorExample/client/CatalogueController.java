package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.CatalogueEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class CatalogueController {

  @FXML // fx:id="cataloguePane"
  private FlowPane cataloguePane; // Value injected by FXMLLoader

  @FXML
  private Label loadingLabel;

  @FXML
  private Button addBtn;

  @Subscribe
  public void displayItems(CatalogueEvent event) {
    List<Item> items = event.getItems();
    Platform.runLater(() -> {
      cataloguePane.getChildren().remove(loadingLabel);
      for (Item item : items) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("item" + ".fxml"));
        Parent itemEntry = null;
        try {
          itemEntry = fxmlLoader.load();
        } catch (IOException e) {
          e.printStackTrace();
        }
        ItemController controller = (ItemController) fxmlLoader.getController();
        controller.setItem(item);
        cataloguePane.getChildren().add(itemEntry);
      }
      addBtn.setDisable(false);
    });
  }

  @FXML
  void addItem(ActionEvent event) {
    try {
      EventBus.getDefault().unregister(this);
      App.setRoot("add");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Subscribe
  public void unsubscribe(UnsubscribeEvent event) {
    EventBus.getDefault().unregister(this);
  }

  @FXML
  void initialize() {
    EventBus.getDefault().register(this);
  }
}
