package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;

public class ItemEvent {
  private Item item;

  public ItemEvent(Item item) {
    this.item = item;
  }

  public Item getItem() {
    return item;
  }
}
