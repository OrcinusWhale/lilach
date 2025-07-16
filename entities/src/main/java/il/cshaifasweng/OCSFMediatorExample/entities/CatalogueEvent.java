package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;
import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class CatalogueEvent implements Serializable {
  private List<Item> items;

  public CatalogueEvent(List<Item> items) {
    this.items = items;
  }

  public List<Item> getItems() {
    return items;
  }
}
