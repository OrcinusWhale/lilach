package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.CatalogueEvent;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox; // added

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import il.cshaifasweng.OCSFMediatorExample.entities.NewItemEvent;

public class CatalogueController {

  private static final boolean DEBUG_LAYOUT = false; // toggle to trace layout recalcs

  @FXML // fx:id="cataloguePane"
  private FlowPane cataloguePane; // Value injected by FXMLLoader

  @FXML
  private Label loadingLabel;

  @FXML
  private Button addBtn;

  @FXML
  private ChoiceBox<String> categoryBox;

  @FXML
  private ChoiceBox<String> storeBox;

  @FXML
  private Button backBtn;

  @FXML
  private Button cartBtn;

  @FXML
  private ScrollPane rootScroll;

  @FXML
  private VBox rootContainer;

  @FXML
  private HBox filterBar; // new reference to filter bar HBox

  private List<Parent> itemEntries = new ArrayList<>();

  private List<Item> items = new ArrayList<>();

  private double lastResponsiveWidth = -1; // throttle responsive recalcs

  @Subscribe
  public void displayItems(CatalogueEvent event) {
    System.out.println("Client received CatalogueEvent");
    List<Item> items = event.getItems();
    System.out.println("CatalogueEvent contains " + items.size() + " items");
    this.items = items;
    Platform.runLater(() -> {
      // Directly remove loading label via filterBar reference
      if (filterBar.getChildren().contains(loadingLabel)) {
        filterBar.getChildren().remove(loadingLabel);
      }
      // Skip index 0 (Custom order placeholder)
      for (int i = 1; i < items.size(); i++) {
        Item item = items.get(i);
        System.out.println("Loading item: " + item.getName());
        loadItem(item);
      }
      addBtn.setDisable(false);
      System.out.println("Catalogue display completed");
    });
  }

  @Subscribe
  public void newItem(NewItemEvent event) {
    Item item = event.getItem();
    items.add(item);
    Platform.runLater(() -> {
      loadItem(item);
    });
  }

  public void loadItem(Item item) {
    ObservableList<String> categories = categoryBox.getItems();
    String category = item.getType();
    if (!categories.contains(category)) {
      categories.add(category);
    }
    String selectedCategory = categoryBox.getValue();
    if (!selectedCategory.equals("All") && !selectedCategory.equals(category)) {
      return;
    }
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("item" + ".fxml"));
    Parent itemEntry = null;
    try {
      itemEntry = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    itemEntries.add(itemEntry);
    ItemController controller = (ItemController) fxmlLoader.getController();
    controller.setItem(item);
    cataloguePane.getChildren().add(itemEntry);
  }

  @FXML
  void filter(ActionEvent event) {
    cataloguePane.getChildren().removeAll(itemEntries);
    itemEntries = new ArrayList<>();
    displayItems(new CatalogueEvent(items));
  }

  @FXML
  void addItem(ActionEvent event) {
    try {
      EventBus.getDefault().unregister(this);
      App.setRoot("add");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  void goBack(ActionEvent event) {
    try {
      EventBus.getDefault().unregister(this);
      App.setRoot("userDetails");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  void viewCart(ActionEvent event) {
    try {
      EventBus.getDefault().unregister(this);
      App.setRoot("cart");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  void customOrder() {
    EventBus.getDefault().unregister(this);
    try {
      App.setRoot("custom");
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Subscribe
  public void unsubscribe(UnsubscribeEvent event) {
    EventBus.getDefault().unregister(this);
  }

  @FXML
  void initialize() {
    EventBus.getDefault().register(this);
    categoryBox.getItems().add("All");
    categoryBox.setValue("All");
    storeBox.getItems().add("All");
    storeBox.setValue("All");

    applyUserPermissions();

    setupResponsiveLayout();

    // Request catalogue data from server
    try {
      System.out.println("Client sending catalogue request to server");
      App.getClient().sendToServer("catalogue");
      System.out.println("Catalogue request sent successfully");
    } catch (IOException e) {
      System.err.println("Error sending catalogue request: " + e.getMessage());
      e.printStackTrace();
      Platform.runLater(() -> {
        loadingLabel.setText("Error loading catalogue");
      });
    }
  }

  private void applyUserPermissions() {
    // Hide add button for customer users
    try {
      if (UserSession.getCurrentUser() != null && UserSession.getCurrentUser().isCustomer()) {
        addBtn.setVisible(false);
        addBtn.setManaged(false); // remove space in layout
      }
    } catch (Exception ex) {
      System.err.println("Failed to apply user permissions: " + ex.getMessage());
    }
  }

  private void setupResponsiveLayout() {
    // Listen only to width changes, not height/viewport adjustments from scrolling
    rootScroll.widthProperty().addListener((obs, oldW, newW) -> updateResponsiveLayout(newW.doubleValue()));
    Platform.runLater(() -> updateResponsiveLayout(rootScroll.getWidth()));
  }

  private void updateResponsiveLayout(double width) {
    if (lastResponsiveWidth >= 0 && Math.abs(width - lastResponsiveWidth) < 1) {
      if (DEBUG_LAYOUT) {
        System.out.println("[Catalogue] Skipping layout recalculation (width delta < 1px): " + width);
      }
      return;
    }
    lastResponsiveWidth = width;
    double horizontalPadding = 48; // VBox padding (24 left + 24 right)
    double contentWidth = Math.max(320, width - horizontalPadding);
    if (Math.abs(rootContainer.getPrefWidth() - contentWidth) > 1) {
      if (DEBUG_LAYOUT) {
        System.out.println("[Catalogue] Updating content width to: " + contentWidth);
      }
      rootContainer.setPrefWidth(contentWidth);
    }
    cataloguePane.setPrefWrapLength(contentWidth);
    applyBreakpointStyles(width);
  }

  private void applyBreakpointStyles(double width) {
    // Remove previous breakpoint classes
    rootContainer.getStyleClass().removeAll("bp-small", "bp-medium", "bp-large");
    if (width < 640) {
      rootContainer.getStyleClass().add("bp-small");
      cataloguePane.setHgap(12);
      cataloguePane.setVgap(16);
    } else if (width < 1024) {
      rootContainer.getStyleClass().add("bp-medium");
      cataloguePane.setHgap(18);
      cataloguePane.setVgap(18);
    } else {
      rootContainer.getStyleClass().add("bp-large");
      cataloguePane.setHgap(20);
      cataloguePane.setVgap(20);
    }
  }
}
