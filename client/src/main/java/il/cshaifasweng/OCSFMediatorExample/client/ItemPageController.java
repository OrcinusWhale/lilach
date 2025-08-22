package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
import il.cshaifasweng.OCSFMediatorExample.entities.UpdateItemEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.CartResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.User;

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

  @FXML
  private Button checkDeleteBtn;

  @FXML
  private Label deleteLabel;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button cancelDeleteBtn;

  @FXML
  private CheckBox saleCheck;

  @FXML
  private TextField saleTF;

  @FXML
  private Label saleLabel;

  @FXML
  private Button addToCartButton;

  @FXML
  private TextField quantityField;

  @FXML
  private TextArea specialRequestsArea;

  @FXML
  private Button viewCartButton;

  @FXML
  private Label addToCartLabel;

  @FXML
  private Label quantityLabel;

  @FXML
  private Label specialRequestsLabel;

  private File selectedImage;

  private FileChooser fileChooser = new FileChooser();

  private boolean edit = false;

  private boolean delete = false;

  @FXML
  void backToCatalogue(ActionEvent event) {
    EventBus.getDefault().unregister(this);
    try {
      // Don't send catalogue request here - let CatalogueController handle it
      App.setRoot("catalogue");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  void addToCart(ActionEvent event) {
    User currentUser = UserSession.getCurrentUser();
    if (currentUser == null) {
      showError("Please log in to add items to cart");
      return;
    }

    if (item == null) {
      showError("Item not loaded");
      return;
    }

    try {
      int quantity = Integer.parseInt(quantityField.getText().trim());
      if (quantity <= 0) {
        showError("Please enter a valid quantity");
        return;
      }

      String specialRequests = specialRequestsArea.getText().trim();
      AddToCartRequest request = new AddToCartRequest(
        currentUser.getUserId(),
        item.getItemId(),
        quantity,
        specialRequests.isEmpty() ? null : specialRequests
      );

      System.out.println("ItemPageController: Sending AddToCartRequest for user " + 
                        currentUser.getUserId() + ", item " + item.getItemId() + 
                        ", quantity " + quantity);
      
      App.getClient().sendToServer(request);
      addToCartButton.setDisable(true);
      addToCartButton.setText("Adding...");

    } catch (NumberFormatException e) {
      showError("Please enter a valid quantity");
    } catch (IOException e) {
      showError("Error adding to cart: " + e.getMessage());
    }
  }

  @FXML
  void viewCart(ActionEvent event) {
    try {
      EventBus.getDefault().unregister(this);
      App.setRoot("cart");
    } catch (IOException e) {
      showError("Error opening cart: " + e.getMessage());
    }
  }

  @Subscribe
  public void onCartResponse(CartResponse response) {
    Platform.runLater(() -> {
      addToCartButton.setDisable(false);
      addToCartButton.setText("Add to Cart");

      System.out.println("ItemPageController: Received CartResponse - Success: " + response.isSuccess());
      
      if (response.isSuccess()) {
        showSuccess("Item added to cart successfully!");
        quantityField.setText("1"); // Reset quantity
        specialRequestsArea.clear(); // Clear special requests
        
        // The CartResponse will automatically be received by CartController
        // due to EventBus, so no additional action needed here
      } else {
        showError(response.getMessage());
      }
    });
  }

  private void showError(String message) {
    Platform.runLater(() -> {
      javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }

  private void showSuccess(String message) {
    Platform.runLater(() -> {
      javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
      alert.setTitle("Success");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
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
    boolean onSale = (item.getSalePrice() != -1);
    saleCheck.setSelected(onSale);
    priceLabel.getStyleClass().remove("strikethrough");
    if (onSale) {
      saleTF.setDisable(!edit);
      saleTF.setVisible(edit);
      saleLabel.setVisible(!edit);
    } else {
      saleTF.setDisable(true);
      saleTF.setVisible(false);
    }
    saleCheck.setDisable(!edit);
    saleCheck.setVisible(edit);
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
  void toggleSale(ActionEvent event) {
    boolean value = saleCheck.isSelected();
    saleTF.setDisable(!value);
    saleTF.setVisible(value);
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
    String sale = saleTF.getText();
    if (!saleCheck.isSelected()) {
      item.setSalePrice(-1);
    } else if (saleCheck.isSelected() && !sale.isEmpty()) {
      try {
        item.setSalePrice(Integer.parseInt(sale));
      } catch (NumberFormatException e) {
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
      App.getClient().sendToServer(item);
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

  @FXML
  void toggleDelete(ActionEvent event) {
    delete = !delete;
    checkDeleteBtn.setVisible(!delete);
    checkDeleteBtn.setDisable(delete);
    deleteLabel.setVisible(delete);
    deleteLabel.setDisable(!delete);
    deleteBtn.setVisible(delete);
    deleteBtn.setDisable(!delete);
    cancelDeleteBtn.setVisible(delete);
    cancelDeleteBtn.setDisable(!delete);
  }

  @FXML
  void deleteItem(ActionEvent event) {
    try {
      App.getClient().sendToServer("delete " + itemId);
    } catch (IOException e) {
      e.printStackTrace();
    }
    backToCatalogue(event);
  }

  @Subscribe
  public void displayItem(UpdateItemEvent event) {
    item = event.getItem();
    String id = Integer.toString(item.getItemId());
    if (id.equals(itemId)) {
      Platform.runLater(() -> {
        idLabel.setText("Item ID: " + id);
        idLabel.setVisible(true);
        nameLabel.setText(item.getName());
        nameTF.setPromptText(item.getName());
        int salePrice = item.getSalePrice();
        priceLabel.setText("Price: " + item.getPrice() + "$");
        priceLabel.setVisible(true);
        
        // Clear any previous styling first
        priceLabel.getStyleClass().remove("strikethrough");
        saleLabel.setVisible(false);
        
        // Only show sale if there's actually a valid sale price (not -1 and different from regular price)
        if (salePrice > 0 && salePrice != item.getPrice()) {
          priceLabel.getStyleClass().add("strikethrough");
          saleLabel.setText("SALE: " + salePrice + "$");
          saleLabel.setVisible(true);
          saleTF.setPromptText("" + salePrice);
        } else {
          saleTF.setPromptText("");
        }
        priceTF.setPromptText("" + item.getPrice());
        typeLabel.setText("Item type: " + item.getType());
        typeLabel.setVisible(true);
        typeTF.setPromptText(item.getType());
        if (!edit) {
          editBtn.setDisable(false);
          editBtn.setVisible(true);
        }
        checkDeleteBtn.setDisable(false);
        checkDeleteBtn.setVisible(true);
        
        // Show cart functionality for customers (including all customer types)
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null && (currentUser.isCustomer() || currentUser.isStoreSpecific() || currentUser.isBrandUser())) {
          addToCartButton.setVisible(true);
          addToCartButton.setDisable(false);
          quantityField.setVisible(true);
          specialRequestsArea.setVisible(true);
          viewCartButton.setVisible(true);
          if (addToCartLabel != null) addToCartLabel.setVisible(true);
          if (quantityLabel != null) quantityLabel.setVisible(true);
          if (specialRequestsLabel != null) specialRequestsLabel.setVisible(true);
        }
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
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp"));
    
    // Initialize cart components
    if (quantityField != null) {
      quantityField.setText("1");
    }
    
    // Show cart functionality for logged in customers
    User currentUser = UserSession.getCurrentUser();
    if (currentUser != null && (currentUser.isCustomer() || currentUser.isStoreSpecific() || currentUser.isBrandUser())) {
      if (addToCartButton != null) addToCartButton.setVisible(true);
      if (quantityField != null) quantityField.setVisible(true);
      if (specialRequestsArea != null) specialRequestsArea.setVisible(true);
      if (viewCartButton != null) viewCartButton.setVisible(true);
      if (addToCartLabel != null) addToCartLabel.setVisible(true);
      if (quantityLabel != null) quantityLabel.setVisible(true);
      if (specialRequestsLabel != null) specialRequestsLabel.setVisible(true);
    } else {
      // Hide cart functionality for non-customers
      if (addToCartButton != null) addToCartButton.setVisible(false);
      if (quantityField != null) quantityField.setVisible(false);
      if (specialRequestsArea != null) specialRequestsArea.setVisible(false);
      if (viewCartButton != null) viewCartButton.setVisible(false);
      if (addToCartLabel != null) addToCartLabel.setVisible(false);
      if (quantityLabel != null) quantityLabel.setVisible(false);
      if (specialRequestsLabel != null) specialRequestsLabel.setVisible(false);
    }
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getItemId() {
    return itemId;
  }
}
