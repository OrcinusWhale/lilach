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

public class SimpleServer extends AbstractServer {
  private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

  public SimpleServer(int port) {
    super(port);

  }

  @Override
  protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
    String msgString = msg.toString();
    Session session = null;
    try {
      SessionFactory sessionFactory = App.getSessionFactory();
      session = sessionFactory.openSession();
      if (msgString.equals("catalogue")) {
        try {
          CriteriaBuilder builder = App.session.getCriteriaBuilder();
          CriteriaQuery<Item> query = builder.createQuery(Item.class);
          query.from(Item.class);
          List<Item> items = App.session.createQuery(query).getResultList();
          client.sendToClient(items);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (msgString.startsWith("get")) {
        try {
          Item item = App.session.get(Item.class, Integer.parseInt(msgString.split(" ")[1]));
          client.sendToClient(item);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (msgString.startsWith("update price")) {
        try {
          String[] splitMsg = msgString.split(" ");
          Item item = App.session.get(Item.class, Integer.parseInt(splitMsg[2]));
          session.beginTransaction();
          item.setPrice(Integer.parseInt(splitMsg[3]));
          session.update(item);
          session.flush();
          session.getTransaction().commit();
          client.sendToClient(item);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (HibernateException e) {
      if (session != null) {
        session.getTransaction().rollback();
      }
      System.err.println("Whoops, rollback");
      e.printStackTrace();
      try {
        client.sendToClient("fail");
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    } finally {
      if (session != null) {
        session.close();
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
