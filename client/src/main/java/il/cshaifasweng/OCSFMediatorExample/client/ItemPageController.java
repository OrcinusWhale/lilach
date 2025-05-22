package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.HashMap;

public class ItemPageController {

  @FXML // fx:id="idLabel"
  private Label idLabel; // Value injected by FXMLLoader

  private String itemId;

  @FXML // fx:id="nameLabel"
  private Label nameLabel; // Value injected by FXMLLoader

  @FXML // fx:id="priceLabel"
  private Label priceLabel; // Value injected by FXMLLoader

  @FXML // fx:id="typeLabel"
  private Label typeLabel; // Value injected by FXMLLoader

  @FXML
  private Button backBtn;

  @FXML
  private Button updateBtn;

  @FXML
  private Label priceErrorLabel;

  @FXML
  private Button confirmPriceBtn;

  @FXML
  private Button cancelPriceBtn;

  @FXML
  private TextField priceTF;

  @FXML
  void backToCatalogue(ActionEvent event) {
    try {
      App.setRoot("catalogue");
      SimpleClient.getClient().sendToServer("catalogue");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void showPriceUpdate(ActionEvent event) {
    updateBtn.setDisable(true);
    updateBtn.setVisible(false);
    priceTF.setDisable(false);
    priceTF.setVisible(true);
    confirmPriceBtn.setDisable(false);
    confirmPriceBtn.setVisible(true);
    cancelPriceBtn.setDisable(false);
    cancelPriceBtn.setVisible(true);
  }

  @FXML
  void updatePrice(ActionEvent event) {
    String newPrice = priceTF.getText();
    try {
      Integer.parseInt(priceTF.getText());
    } catch (Exception e) {
      priceErrorLabel.setVisible(true);
      return;
    }
    try {
      SimpleClient.getClient().sendToServer("update price " + itemId + " " + newPrice);
      cancelPrice(event);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void cancelPrice(ActionEvent event) {
    updateBtn.setDisable(false);
    updateBtn.setVisible(true);
    priceTF.setDisable(true);
    priceTF.setVisible(false);
    confirmPriceBtn.setDisable(true);
    confirmPriceBtn.setVisible(false);
    cancelPriceBtn.setDisable(true);
    cancelPriceBtn.setVisible(false);
    priceErrorLabel.setVisible(false);
  }

  public void displayItem(HashMap<String, String> item) {
    Platform.runLater(() -> {
      idLabel.setText("Item ID: " + item.get("itemId"));
      idLabel.setVisible(true);
      nameLabel.setText(item.get("name"));
      priceLabel.setText("Price: " + item.get("price"));
      priceLabel.setVisible(true);
      typeLabel.setText("Item type: " + item.get("type"));
      typeLabel.setVisible(true);
      backBtn.setDisable(false);
      updateBtn.setDisable(false);
      updateBtn.setVisible(true);
    });
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getItemId() {
    return itemId;
  }
}
