package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;

import java.io.*;
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
  private String imageFileName;
  @Transient
  private byte[] image;

  public Item(String name, String type, int price) {
    this.name = name;
    this.type = type;
    this.price = price;
  }

  public Item(String name, String type, int price, String imageFileName) {
    this.name = name;
    this.type = type;
    this.price = price;
    this.imageFileName = imageFileName;
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

  public ByteArrayInputStream getImage() {
    return new ByteArrayInputStream(image);
  }

  public void loadImage() {
    File imageFile = new File("images", imageFileName);
    image = new byte[(int) imageFile.length()];
    try {
      FileInputStream inputStream = new FileInputStream(imageFile);
      inputStream.read(image);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
