package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class ItemEvent implements Serializable {
  private Item item;

  public ItemEvent(Item item) {
    this.item = item;
  }

  public Item getItem() {
    return item;
  }
}
