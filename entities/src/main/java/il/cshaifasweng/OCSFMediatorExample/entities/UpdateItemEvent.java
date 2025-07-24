package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class UpdateItemEvent implements Serializable {
  private Item item;

  public UpdateItemEvent(Item item) {
    this.item = item;
  }

  public Item getItem() {
    return item;
  }
}
