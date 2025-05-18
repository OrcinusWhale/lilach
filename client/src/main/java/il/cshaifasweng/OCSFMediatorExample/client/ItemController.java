package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;

import java.io.IOException;

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
      App.setRoot("itemPage");
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
}
