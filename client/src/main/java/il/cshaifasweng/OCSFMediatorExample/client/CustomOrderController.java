/**
 * Sample Skeleton for 'custom.fxml' Controller Class
 */

package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CustomOrderController {

  @FXML // fx:id="colorTF"
  private TextField colorTF; // Value injected by FXMLLoader

  @FXML // fx:id="descTF"
  private TextArea descTF; // Value injected by FXMLLoader

  @FXML // fx:id="itemTF"
  private TextField itemTF; // Value injected by FXMLLoader

  @FXML // fx:id="priceTF1"
  private TextField priceTF1; // Value injected by FXMLLoader

  @FXML // fx:id="priceTF2"
  private TextField priceTF2; // Value injected by FXMLLoader

  @FXML
  private Label itemError;

  @FXML
  private Label priceError;

  @FXML
  void addToCart(ActionEvent event) {
    boolean error = false;
    String itemString = itemTF.getText();
    String priceString1 = priceTF1.getText();
    String priceString2 = priceTF2.getText();
    String color = colorTF.getText();
    String desc = descTF.getText();
    int price1 = 0;
    int price2 = 0;
    if (itemString.isEmpty()) {
      itemError.setVisible(true);
      error = true;
    } else {
      itemError.setVisible(false);
    }
    try {
      price1 = Integer.parseInt(priceString1);
      price2 = Integer.parseInt(priceString2);
      if (price1 < 0 || price2 < 0 || price2 < price1) {
        priceError.setVisible(true);
        error = true;
      } else {
        priceError.setVisible(false);
      }
    } catch (NumberFormatException e) {
      priceError.setVisible(true);
      error = true;
    }
    if (error) {
      return;
    }

  }

  @FXML
  void backToCatalogue(ActionEvent event) {
    try {
      App.setRoot("catalogue");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
