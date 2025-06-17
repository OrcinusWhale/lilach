package il.cshaifasweng.OCSFMediatorExample.client;

import java.util.HashMap;
import java.util.List;

public class CatalogueEvent {
  private List<HashMap<String, String>> items;

  public CatalogueEvent(List<HashMap<String, String>> items) {
    this.items = items;
  }

  public List<HashMap<String, String>> getItems() {
    return items;
  }
}
