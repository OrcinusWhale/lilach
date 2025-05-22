package il.cshaifasweng.OCSFMediatorExample.client;

import java.util.List;
import java.util.HashMap;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

public class SimpleClient extends AbstractClient {

  private static SimpleClient client = null;
  private static String host = "localhost";

  private SimpleClient(String host, int port) {
    super(host, port);
  }

  @Override
  protected void handleMessageFromServer(Object msg) {
    if (msg instanceof List) {
      List<HashMap<String, String>> items = (List<HashMap<String, String>>) msg;
      CatalogueController controller = (CatalogueController) App.getFxmlLoader().getController();
      controller.displayItems(items);
    } else if (msg instanceof HashMap) {
      HashMap<String, String> item = (HashMap<String, String>) msg;
      if (App.getDisplayed().equals("catalogue")) {
        CatalogueController controller = (CatalogueController) App.getFxmlLoader().getController();
        controller.updateItem(item);
      } else {
        ItemPageController controller = App.getFxmlLoader().getController();
        if (controller.getItemId().equals(item.get("itemId"))) {
          controller.displayItem(item);
        }
      }
    }
  }

  public static SimpleClient getClient() {
    if (client == null) {
      client = new SimpleClient(host, 3000);
    }
    return client;
  }

  public static void setHostIp(String host) {
    SimpleClient.host = host;
  }
}
