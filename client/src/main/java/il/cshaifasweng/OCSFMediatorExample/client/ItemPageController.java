package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class ItemPageController {

  private Item item;

  @FXML
  private ImageView imageView;

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
  private Button editBtn;

  @FXML
  private Label priceErrorLabel;

  @FXML
  private Button confirmBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private TextField nameTF;

  @FXML
  private TextField typeTF;

  @FXML
  private TextField priceTF;

  @FXML
  private CheckBox imageCheck;

  @FXML
  private Button browseBtn;

  @FXML
  private Label selectedImageLabel;

  private File selectedImage;

  private FileChooser fileChooser = new FileChooser();

  private boolean edit = false;

  @FXML
  void backToCatalogue(ActionEvent event) {
    EventBus.getDefault().unregister(this);
    try {
      App.setRoot("catalogue");
      SimpleClient.getClient().sendToServer("catalogue");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void toggleEdit(ActionEvent event) {
    edit = !edit;
    editBtn.setDisable(edit);
    editBtn.setVisible(!edit);
    nameTF.setDisable(!edit);
    nameTF.setVisible(edit);
    typeTF.setDisable(!edit);
    typeTF.setVisible(edit);
    priceTF.setDisable(!edit);
    priceTF.setVisible(edit);
    imageCheck.setDisable(!edit);
    imageCheck.setVisible(edit);
    browseBtn.setDisable(!edit);
    browseBtn.setVisible(edit);
    selectedImageLabel.setVisible(edit);
    confirmBtn.setDisable(!edit);
    confirmBtn.setVisible(edit);
    cancelBtn.setDisable(!edit);
    cancelBtn.setVisible(edit);
    imageView.setVisible(!edit);
    priceErrorLabel.setVisible(false);
  }

  @FXML
  void confirmEdit(ActionEvent event) {
    String price = priceTF.getText();
    if (!price.isEmpty()) {
      try {
        item.setPrice(Integer.parseInt(price));
      } catch (Exception e) {
        priceErrorLabel.setVisible(true);
        return;
      }
    }
    if (imageCheck.isSelected() && selectedImage != null) {
      item.setImageFile(selectedImage);
      item.loadImage();
      item.setImageFile(null);
    } else if (!imageCheck.isSelected()) {
      item.setImageFile(null);
      item.setImage(null);
    }
    String name = nameTF.getText();
    if (!name.isEmpty()) {
      item.setName(name);
    }
    String type = typeTF.getText();
    if (!type.isEmpty()) {
      item.setType(type);
    }
    try {
      SimpleClient.getClient().sendToServer(item);
      toggleEdit(event);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void toggleBrowse(ActionEvent event) {
    boolean value = imageCheck.isSelected();
    browseBtn.setDisable(!value);
    browseBtn.setVisible(value);
    selectedImageLabel.setVisible(value);
  }

  @FXML
  void browseImage(ActionEvent event) {
    File selectedImage = fileChooser.showOpenDialog(App.getStage());
    if (selectedImage != null) {
      this.selectedImage = selectedImage;
      selectedImageLabel.setText(selectedImage.getName());
    }
  }

  @Subscribe
  public void displayItem(ItemEvent event) {
    item = event.getItem();
    String id = Integer.toString(item.getItemId());
    if (id.equals(itemId)) {
      Platform.runLater(() -> {
        idLabel.setText("Item ID: " + id);
        idLabel.setVisible(true);
        nameLabel.setText(item.getName());
        nameTF.setPromptText(item.getName());
        priceLabel.setText("Price: " + item.getPrice() + "$");
        priceLabel.setVisible(true);
        priceTF.setPromptText("" + item.getPrice());
        typeLabel.setText("Item type: " + item.getType());
        typeLabel.setVisible(true);
        typeTF.setPromptText(item.getType());
        editBtn.setDisable(false);
        editBtn.setVisible(true);
        byte[] image = item.getImage();
        if (image != null) {
          imageView.setImage(new Image(new ByteArrayInputStream(image)));
        } else {
          imageView.setImage(null);
        }
      });
    }
  }

  @FXML
  void initialize() {
    EventBus.getDefault().register(this);
    fileChooser.setTitle("Choose image");
    fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp")
    );
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getItemId() {
    return itemId;
  }
}
