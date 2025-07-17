package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ItemEvent;
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

  public void setItem(Item item) {
    itemId = Integer.toString(item.getItemId());
    nameLabel.setText(item.getName());
    priceLabel.setText(item.getPrice() + "$");
    int sale = item.getSalePrice();
    if (sale == -1) {
      saleLabel.setVisible(false);
      priceLabel.getStyleClass().remove("strikethrough");
    } else {
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
  public void updateItem(ItemEvent event) {
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
