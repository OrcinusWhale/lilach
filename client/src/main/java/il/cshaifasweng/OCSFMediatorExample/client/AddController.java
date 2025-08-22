/**
 * Sample Skeleton for 'add.fxml' Controller Class
 */

package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.AddResponseEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;

public class AddController {

    @FXML // fx:id="backBtn"
    private Button backBtn; // Value injected by FXMLLoader

    @FXML // fx:id="browseBtn"
    private Button browseBtn; // Value injected by FXMLLoader

    @FXML // fx:id="confirmBtn"
    private Button confirmBtn; // Value injected by FXMLLoader

    @FXML // fx:id="imageCheck"
    private CheckBox imageCheck; // Value injected by FXMLLoader

    @FXML // fx:id="nameTF"
    private TextField nameTF; // Value injected by FXMLLoader

    @FXML // fx:id="priceTF"
    private TextField priceTF; // Value injected by FXMLLoader

    @FXML // fx:id="selectedImageLabel"
    private Label selectedImageLabel; // Value injected by FXMLLoader

    @FXML // fx:id="typeTF"
    private TextField typeTF; // Value injected by FXMLLoader

    @FXML
    private Label nameError;

    @FXML
    private Label typeError;

    @FXML
    private Label priceError;

    @FXML
    private Label successLabel;

    @FXML
    private CheckBox saleCheck;

    @FXML
    private TextField saleTF;

    private FileChooser fileChooser = new FileChooser();

    private File selectedImage;

    @FXML
    void addItem(ActionEvent event) {
        successLabel.setVisible(false);
        boolean error = false;
        String name = nameTF.getText();
        String type = typeTF.getText();
        String priceString = priceTF.getText();
        String saleString = saleTF.getText();
        int price = 0;
        int sale = -1;
        if (name.isEmpty()) {
            nameError.setVisible(true);
            error = true;
        }
        if (type.isEmpty()) {
            typeError.setVisible(true);
            error = true;
        }
        if (priceString.isEmpty()) {
            priceError.setVisible(true);
            error = true;
        }
        try {
            price = Integer.parseInt(priceString);
        } catch (NumberFormatException e) {
            priceError.setVisible(true);
            error = true;
        }
        if (!saleString.isEmpty()) {
           try {
               sale = Integer.parseInt(saleString);
           } catch (NumberFormatException e) {
               priceError.setVisible(true);
               error = true;
           }
        }
        if (error) {
            return;
        }
        nameError.setVisible(false);
        typeError.setVisible(false);
        priceError.setVisible(false);
        Item item = new Item(name, type, price);
        item.setSalePrice(sale);
        if (imageCheck.isSelected() && selectedImage != null) {
            item.setImageFile(selectedImage);
            item.loadImage();
            item.setImageFile(null);
        }
        try {
            App.getClient().sendToServer(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
    void browseImage(ActionEvent event) {
        File selectedImage = fileChooser.showOpenDialog(App.getStage());
        if (selectedImage != null) {
            this.selectedImage = selectedImage;
            selectedImageLabel.setText(selectedImage.getName());
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
    void toggleSale(ActionEvent event) {
        boolean value = saleCheck.isSelected();
        saleTF.setDisable(!value);
        saleTF.setVisible(value);
    }

    @FXML
    void initialize() {
        EventBus.getDefault().register(this);
        fileChooser.setTitle("Choose image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp")
        );
    }

    @Subscribe
    public void showResponse(AddResponseEvent event) {
        String response = event.getResponse();
        if (response.equals("add success")) {
            successLabel.setVisible(true);
        }
    }
}
