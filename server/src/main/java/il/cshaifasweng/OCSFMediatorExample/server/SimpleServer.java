package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.AddResponseEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.CatalogueEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.NewItemEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.UpdateItemEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.HibernateException;

public class SimpleServer extends AbstractServer {
  private static final ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

  public SimpleServer(int port) {
    super(port);
  }

  @Override
  protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    if (msg instanceof String) {
      String msgString = (String) msg;
      System.out.println(msg);
      if (msgString.equals("catalogue")) {
        CriteriaBuilder builder = App.session.getCriteriaBuilder();
        CriteriaQuery<Item> query = builder.createQuery(Item.class);
        query.from(Item.class);
        List<Item> items = App.session.createQuery(query).getResultList();
        for (Item item : items) {
          item.loadImage();
        }
        try {
          client.sendToClient(new CatalogueEvent(items));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (msgString.startsWith("get")) {
        Item item = App.session.get(Item.class, Integer.parseInt(msgString.split(" ")[1]));
        item.loadImage();
        try {
          client.sendToClient(new UpdateItemEvent(item));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (msgString.startsWith("delete")) {
        Item item = App.session.get(Item.class, Integer.parseInt(msgString.split(" ")[1]));
        File imageFile = item.getImageFile();
        try {
          if (imageFile != null) {
            imageFile.delete();
          }
          App.session.beginTransaction();
          App.session.delete(item);
          App.session.flush();
          App.session.getTransaction().commit();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (msgString.equals("add")) {
        SubscribersList.add(new SubscribedClient(client));
      } else if (msgString.equals("remove")) {
        SubscribersList.removeIf(subscribedClient -> subscribedClient.getClient().equals(client));
      }
    } else if (msg instanceof Item) {
      Item item = (Item) msg;
      int id = item.getItemId();
      byte[] image = item.getImage();
      if (image != null && item.getImageFile() == null) {
        try {
          File imageFile = File.createTempFile("image", ".tmp", new File("images"));
          new FileOutputStream(imageFile).write(image);
          item.setImageFile(imageFile);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      Item dbItem;
      boolean newItem = false;
      if (item.getItemId() == -1) {
        dbItem = new Item();
        newItem = true;
      } else {
        dbItem = App.session.get(Item.class, id);
      }
      dbItem.setName(item.getName());
      dbItem.setImageFile(item.getImageFile());
      dbItem.setType(item.getType());
      dbItem.setPrice(item.getPrice());
      dbItem.setSalePrice(item.getSalePrice());
      try {
        App.session.beginTransaction();
        App.session.saveOrUpdate(dbItem);
        App.session.flush();
        App.session.getTransaction().commit();
        if (newItem) {
          sendToAllClients(new NewItemEvent(item));
        } else {
          sendToAllClients(new UpdateItemEvent(item));
        }
        client.sendToClient(new AddResponseEvent("add success"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void sendToAllClients(Object message) {
    try {
      for (SubscribedClient subscribedClient : SubscribersList) {
        subscribedClient.getClient().sendToClient(message);
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}
