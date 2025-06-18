package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashMap;

@Entity
@Table(name = "Items")
public class Item implements Serializable {
  private static final long serialVersionUID = 1L;

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

  public Item() {

  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getItemId() {
    return itemId;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public int getPrice() {
    return price;
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
