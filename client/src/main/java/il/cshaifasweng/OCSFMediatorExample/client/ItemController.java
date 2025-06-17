package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.HashMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class ItemController {
  private String itemId;

  @FXML // fx:id="nameLabel"
  private Label nameLabel; // Value injected by FXMLLoader

  @FXML // fx:id="priceLabel"
  private Label priceLabel; // Value injected by FXMLLoader

  @FXML // fx:id="typeLabel"
  private Label typeLabel; // Value injected by FXMLLoader

  @FXML
  void loadItem(MouseEvent event) {
    try {
      EventBus.getDefault().post(new UnsubscribeEvent());
      App.setRoot("itemPage");
      ItemPageController controller = App.getFxmlLoader().getController();
      controller.setItemId(itemId);
      SimpleClient.getClient().sendToServer("get " + itemId);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public void setName(String name) {
    nameLabel.setText(name);
  }

  public void setPrice(String price) {
    priceLabel.setText(price);
  }

  public void setType(String type) {
    typeLabel.setText(type);
  }

  public void setItem(HashMap<String, String> item) {
    itemId = item.get("itemId");
    nameLabel.setText(item.get("name"));
    priceLabel.setText(item.get("price"));
    typeLabel.setText(item.get("type"));
  }

  @Subscribe
  public void updatePrice(ItemEvent event) {
    HashMap<String, String> item = event.getItem();
    if (item.get("itemId").equals(itemId)) {
      Platform.runLater(() -> {
        priceLabel.setText(item.get("price"));
      });
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

  public String getItemId() {
    return itemId;
  }
}
