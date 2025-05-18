package il.cshaifasweng.OCSFMediatorExample.server;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@Table(name = "Items")
public class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int itemId;
  private String name;
  private String type;
  private int price;

  public Item(String name, String type, int price) {
    this.name = name;
    this.type = type;
    this.price = price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public HashMap<String, String> toHashMap() {
    HashMap<String, String> map = new HashMap<>();
    map.put("itemId", Integer.toString(itemId));
    map.put("name", name);
    map.put("type", type);
    map.put("price", Integer.toString(price) + "$");
    return map;
  }
}
