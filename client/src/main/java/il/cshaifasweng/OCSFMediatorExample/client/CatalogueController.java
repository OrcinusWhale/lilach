package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class CatalogueController {

  @FXML // fx:id="cataloguePane"
  private FlowPane cataloguePane; // Value injected by FXMLLoader

  @FXML
  private Label loadingLabel;

  private ArrayList<ItemController> itemControllers = new ArrayList<>();

  public void displayItems(List<HashMap<String, String>> items) {
    Platform.runLater(() -> {
      cataloguePane.getChildren().remove(loadingLabel);
      for (HashMap<String, String> item : items) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("item" + ".fxml"));
        Parent itemEntry = null;
        try {
          itemEntry = fxmlLoader.load();
        } catch (IOException e) {
          e.printStackTrace();
        }
        ItemController controller = (ItemController) fxmlLoader.getController();
        controller.setItemId(item.get("itemId"));
        controller.setName(item.get("name"));
        controller.setType(item.get("type"));
        controller.setPrice(item.get("price"));
        cataloguePane.getChildren().add(itemEntry);
        itemControllers.add(controller);
      }
    });
  }

  public void updateItem(HashMap<String, String> item) {
    for (ItemController controller : itemControllers) {
      if (controller.getItemId().equals(item.get("itemId"))) {
        Platform.runLater(() -> {
          controller.setPrice(item.get("price"));
        });
        break;
      }
    }
  }
}
