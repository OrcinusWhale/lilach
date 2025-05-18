package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.HashMap;

public class SimpleServer extends AbstractServer {
  private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

  public SimpleServer(int port) {
    super(port);

  }

  @Override
  protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    String msgString = msg.toString();
    System.out.println(msgString);
    if (msgString.equals("catalogue")) {
      System.out.println("hello");
      CriteriaBuilder builder = App.session.getCriteriaBuilder();
      CriteriaQuery<Item> query = builder.createQuery(Item.class);
      query.from(Item.class);
      List<Item> items = App.session.createQuery(query).getResultList();
      List<HashMap<String, String>> response = new ArrayList<>();
      for (Item item : items) {
        response.add(item.toHashMap());
      }
      try {
        client.sendToClient(response);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (msgString.startsWith("get")) {
      HashMap<String, String> item = App.session.get(Item.class, Integer.parseInt(msgString.split(" ")[1])).toHashMap();
      try {
        client.sendToClient(item);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (msgString.startsWith("update price")) {
      String[] splitMsg = msgString.split(" ");
      Item item = App.session.get(Item.class, Integer.parseInt(splitMsg[2]));
      try {
        App.session.beginTransaction();
        item.setPrice(Integer.parseInt(splitMsg[3]));
        App.session.update(item);
        App.session.flush();
        App.session.getTransaction().commit();
        try {
          client.sendToClient(item.toHashMap());
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (HibernateException exception) {
        App.session.getTransaction().rollback();
        exception.printStackTrace();
      }
    }
  }

  public void sendToAllClients(String message) {
    try {
      for (SubscribedClient subscribedClient : SubscribersList) {
        subscribedClient.getClient().sendToClient(message);
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}
