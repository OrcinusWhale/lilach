/**
 * Sample Skeleton for 'add.fxml' Controller Class
 */

package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.AddResponseEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import il.cshaifasweng.OCSFMediatorExample.entities.Store;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

    @FXML
    private AnchorPane root;

    @FXML
    private VBox storesBox; // container for dynamic store checkboxes

    private FileChooser fileChooser = new FileChooser();

    private File selectedImage;

    // Helper methods for styling errors
    private void showFieldError(TextField tf, Label errorLabel) {
        if (!tf.getStyleClass().contains("error-field")) {
            tf.getStyleClass().add("error-field");
        }
        errorLabel.setVisible(true);
    }

    private void clearFieldError(TextField tf, Label errorLabel) {
        tf.getStyleClass().remove("error-field");
        errorLabel.setVisible(false);
    }

    private void applyBreakpoints(double width) {
        var classes = root.getStyleClass();
        classes.removeAll("compact", "ultra-compact");
        if (width < 520) {
            if (!classes.contains("ultra-compact")) classes.add("ultra-compact");
        } else if (width < 640) {
            if (!classes.contains("compact")) classes.add("compact");
        }
    }

    @FXML
    void addItem(ActionEvent event) {
        successLabel.setVisible(false);
        boolean error = false;
        String name = nameTF.getText().trim();
        String type = typeTF.getText().trim();
        String priceString = priceTF.getText().trim();
        String saleString = saleTF.getText().trim();
        int price = 0;
        int sale = -1;

        // Reset previous errors
        clearFieldError(nameTF, nameError);
        clearFieldError(typeTF, typeError);
        clearFieldError(priceTF, priceError);

        if (name.isEmpty()) {
            showFieldError(nameTF, nameError);
            error = true;
        }
        if (type.isEmpty()) {
            showFieldError(typeTF, typeError);
            error = true;
        }
        if (priceString.isEmpty()) {
            showFieldError(priceTF, priceError);
            error = true;
        } else {
            try {
                price = Integer.parseInt(priceString);
                if (price <= 0) {
                    showFieldError(priceTF, priceError);
                    error = true;
                }
            } catch (NumberFormatException e) {
                showFieldError(priceTF, priceError);
                error = true;
            }
        }
        if (!error && saleCheck.isSelected() && !saleString.isEmpty()) {
            try {
                sale = Integer.parseInt(saleString);
                if (sale <= 0 || sale >= price) {
                    // reuse price error label for invalid sale constraints
                    showFieldError(priceTF, priceError);
                    error = true;
                }
            } catch (NumberFormatException e) {
                showFieldError(priceTF, priceError);
                error = true;
            }
        }
        if (error) {
            return;
        }
        Item item = new Item(name, type, price);
        item.setSalePrice(sale);
        // Attach selected stores
        if (storesBox != null) {
            for (Node node : storesBox.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    if (cb.isSelected() && cb.getUserData() instanceof Store) {
                        item.addStore((Store) cb.getUserData());
                    }
                }
            }
        }
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

        // Responsive width listener
        root.widthProperty().addListener((obs, ov, nv) -> applyBreakpoints(nv.doubleValue()));
        applyBreakpoints(root.getPrefWidth());

        // Dynamic error clearing on user input
        nameTF.textProperty().addListener((o, ov, nv) -> { if (!nv.isBlank()) clearFieldError(nameTF, nameError); });
        typeTF.textProperty().addListener((o, ov, nv) -> { if (!nv.isBlank()) clearFieldError(typeTF, typeError); });
        priceTF.textProperty().addListener((o, ov, nv) -> { if (!nv.isBlank()) clearFieldError(priceTF, priceError); });
        saleTF.textProperty().addListener((o, ov, nv) -> { if (!nv.isBlank()) clearFieldError(priceTF, priceError); });

        // Initialize sale/image toggles so hidden state respects checkbox
        toggleSale(null);
        toggleBrowse(null);

        // Load stores list from server
        try {
            App.getClient().sendToServer(new StoreListRequest());
        } catch (IOException e) {
            System.err.println("Failed to request stores: " + e.getMessage());
        }
    }

    @Subscribe
    public void showResponse(AddResponseEvent event) {
        String response = event.getResponse();
        if (response.equals("add success")) {
            successLabel.setVisible(true);
        }
    }

    @Subscribe
    public void onStoreListResponse(StoreListResponse response) {
        Platform.runLater(() -> {
            if (storesBox == null) return;
            storesBox.getChildren().clear();
            if (response.isSuccess() && response.getStores() != null) {
                for (Store store : response.getStores()) {
                    CheckBox cb = new CheckBox(store.getStoreName());
                    cb.setUserData(store); // keep reference for later
                    cb.setSelected(true); // default: available everywhere
                    storesBox.getChildren().add(cb);
                }
            } else {
                Label err = new Label("Failed to load stores");
                err.getStyleClass().add("feedback-label");
                storesBox.getChildren().add(err);
            }
        });
    }
}
