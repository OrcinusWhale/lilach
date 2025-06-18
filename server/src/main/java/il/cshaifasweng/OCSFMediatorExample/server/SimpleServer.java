package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.HibernateException;

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
      CriteriaBuilder builder = App.session.getCriteriaBuilder();
      CriteriaQuery<Item> query = builder.createQuery(Item.class);
      query.from(Item.class);
      List<Item> items = App.session.createQuery(query).getResultList();
      try {
        client.sendToClient(items);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (msgString.startsWith("get")) {
      Item item = App.session.get(Item.class, Integer.parseInt(msgString.split(" ")[1]));
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
        sendToAllClients(item);
      } catch (HibernateException exception) {
        App.session.getTransaction().rollback();
        exception.printStackTrace();
      }
    } else if (msgString.equals("add")) {
      SubscribersList.add(new SubscribedClient(client));
    } else if (msgString.equals("remove")) {
      SubscribersList.removeIf(subscribedClient -> subscribedClient.getClient().equals(client));
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
