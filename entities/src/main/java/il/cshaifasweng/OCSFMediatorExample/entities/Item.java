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
  private int itemId = -1;
  private String name;
  private String type;
  private int price;
  private int salePrice = -1;
  private File imageFile;
  @Transient
  private byte[] image;

  public Item(String name, String type, int price) {
    this.name = name;
    this.type = type;
    this.price = price;
  }

  public Item(String name, String type, int price, File imageFile) {
    this.name = name;
    this.type = type;
    this.price = price;
    this.imageFile = imageFile;
  }

  public Item(String name, String type, int price, byte[] image) {
    this.name = name;
    this.type = type;
    this.price = price;
    this.image = image;
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

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getPrice() {
    return price;
  }

  public void setSalePrice(int salePrice) {
    this.salePrice = salePrice;
  }

  public int getSalePrice() {
    return salePrice;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public void setImageFile(File imageFile) {
    this.imageFile = imageFile;
  }

  public File getImageFile() {
    return imageFile;
  }

  public void loadImage() {
    if (imageFile == null) {
      image = null;
    } else {
      image = new byte[(int) imageFile.length()];
      try {
        FileInputStream inputStream = new FileInputStream(imageFile);
        inputStream.read(image);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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
