package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.UpdateItemEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class ItemController {
  private String itemId;

  @FXML
  private ImageView imageView;

  @FXML // fx:id="nameLabel"
  private Label nameLabel; // Value injected by FXMLLoader

  @FXML // fx:id="priceLabel"
  private Label priceLabel; // Value injected by FXMLLoader

  @FXML
  private Label saleLabel;

  @FXML // fx:id="typeLabel"
  private Label typeLabel; // Value injected by FXMLLoader

  @FXML
  void loadItem(MouseEvent event) {
    try {
      System.out.println("ItemController.loadItem() called for item ID: " + itemId);
      if (itemId == null || itemId.isEmpty()) {
        System.err.println("ERROR: itemId is null or empty!");
        return;
      }
      
      EventBus.getDefault().post(new UnsubscribeEvent());
      App.setRoot("itemPage");
      ItemPageController controller = App.getFxmlLoader().getController();
      controller.setItemId(itemId);
      System.out.println("Sending get request to server for item ID: " + itemId);
      App.getClient().sendToServer("get " + itemId);
      System.out.println("Get request sent successfully");
    } catch (IOException e) {
      System.err.println("Error in loadItem(): " + e.getMessage());
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

  public void setItem(Item item) {
    itemId = Integer.toString(item.getItemId());
    nameLabel.setText(item.getName());
    priceLabel.setText(item.getPrice() + "$");
    int sale = item.getSalePrice();
    
    // Clear any previous styling first
    priceLabel.getStyleClass().remove("strikethrough");
    saleLabel.setVisible(false);
    
    // Only show sale if there's actually a valid sale price (not -1 and not 0)
    if (sale > 0 && sale != item.getPrice()) {
      saleLabel.setText(sale + "$");
      saleLabel.setVisible(true);
      priceLabel.getStyleClass().add("strikethrough");
    }
    
    typeLabel.setText(item.getType());
    byte[] image = item.getImage();
    if (image != null) {
      imageView.setImage(new Image(new ByteArrayInputStream(image)));
    }
  }

  @Subscribe
  public void updateItem(UpdateItemEvent event) {
    Item item = event.getItem();
    if (Integer.toString(item.getItemId()).equals(itemId)) {
      Platform.runLater(() -> {
        setItem(item);
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
