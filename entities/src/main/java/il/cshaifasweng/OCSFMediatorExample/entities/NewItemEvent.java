package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class NewItemEvent implements Serializable {
  private Item item;

  public NewItemEvent(Item item) {
    this.item = item;
  }

  public Item getItem() {
    return item;
  }
}
