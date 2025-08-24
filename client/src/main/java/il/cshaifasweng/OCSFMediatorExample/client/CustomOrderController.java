/**
 * Sample Skeleton for 'custom.fxml' Controller Class
 */

package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.CartResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.User;

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
  private Label requestError; // new general error label

  @FXML
  private Label requestSuccess; // success label

  @FXML
  void addToCart(ActionEvent event) {
    // Reset feedback labels
    if (requestError != null) requestError.setVisible(false);
    if (requestSuccess != null) requestSuccess.setVisible(false);

    boolean error = false;
    String itemString = itemTF.getText();
    String priceString1 = priceTF1.getText();
    String priceString2 = priceTF2.getText();
    String color = colorTF.getText();
    String desc = descTF.getText();
    int price1 = 0;
    int price2 = 0;

    // Validate mandatory item name
    if (itemString == null || itemString.trim().isEmpty()) {
      itemError.setVisible(true);
      error = true;
    } else {
      itemError.setVisible(false);
    }

    // Validate price range if both provided (both expected)
    try {
      price1 = Integer.parseInt(priceString1.trim());
      price2 = Integer.parseInt(priceString2.trim());
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

    User currentUser = UserSession.getCurrentUser();
    if (currentUser == null) {
      if (requestError != null) {
        requestError.setText("Please log in first");
        requestError.setVisible(true);
      }
      return;
    }

    if (error) {
      if (requestError != null) {
        requestError.setText("Please fix validation errors");
        requestError.setVisible(true);
      }
      return;
    }

    // Build special requests string (concatenation of fields)
    StringBuilder sb = new StringBuilder();
    sb.append("Item: ").append(itemString.trim());
    sb.append(" | Price Range: ").append(price1).append("-").append(price2);
    if (color != null && !color.trim().isEmpty()) {
      sb.append(" | Color: ").append(color.trim());
    }
    if (desc != null && !desc.trim().isEmpty()) {
      sb.append(" | Description: ").append(desc.trim());
    }
    String specialRequests = sb.toString();

    // itemId=1 and quantity=1 as per requirement
    AddToCartRequest request = new AddToCartRequest(
        currentUser.getUserId(),
        1,
        1,
        specialRequests
    );

    try {
      App.getClient().sendToServer(request);
    } catch (IOException e) {
      if (requestError != null) {
        requestError.setText("Error sending request: " + e.getMessage());
        requestError.setVisible(true);
      }
    }
  }

  @Subscribe
  public void onCartResponse(CartResponse response) {
    // Only handle responses related to the special item id=1 to avoid noise
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        if (requestSuccess != null) {
          requestSuccess.setText("Custom request added to cart");
          requestSuccess.setVisible(true);
        }
        if (requestError != null) {
          requestError.setVisible(false);
        }
      } else {
        if (requestError != null) {
          requestError.setText(response.getMessage() != null ? response.getMessage() : "Failed to add to cart");
          requestError.setVisible(true);
        }
        if (requestSuccess != null) {
          requestSuccess.setVisible(false);
        }
      }
    });
  }

  @FXML
  void backToCatalogue(ActionEvent event) {
    try {
      EventBus.getDefault().unregister(this);
      App.setRoot("catalogue");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void initialize() {
    EventBus.getDefault().register(this);
    if (requestError != null) requestError.setVisible(false);
    if (requestSuccess != null) requestSuccess.setVisible(false);
  }
}
