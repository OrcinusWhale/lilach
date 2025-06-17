package il.cshaifasweng.OCSFMediatorExample.client;

import java.util.HashMap;

public class ItemEvent {
  private HashMap<String, String> item;

  public ItemEvent(HashMap<String, String> item) {
    this.item = item;
  }

  public HashMap<String, String> getItem() {
    return item;
  }
}
