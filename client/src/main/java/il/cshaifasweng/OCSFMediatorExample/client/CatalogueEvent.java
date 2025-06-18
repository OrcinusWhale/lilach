package il.cshaifasweng.OCSFMediatorExample.client;

import java.util.List;
import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class CatalogueEvent {
  private List<Item> items;

  public CatalogueEvent(List<Item> items) {
    this.items = items;
  }

  public List<Item> getItems() {
    return items;
  }
}
