package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.AddResponseEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.CatalogueEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.NewItemEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.UpdateItemEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.SubscriptionRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.SubscriptionResponse;
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
        dbItem.loadImage();
        if (newItem) {
          sendToAllClients(new NewItemEvent(dbItem));
        } else {
          sendToAllClients(new UpdateItemEvent(dbItem));
        }
        client.sendToClient(new AddResponseEvent("add success"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (msg instanceof User) {
      // Handle user registration
      User newUser = (User) msg;
      System.out.println("Server received user registration request for: " + newUser.getUsername());
      
      try {
        // Check if user already exists
        CriteriaBuilder builder = App.session.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        query.from(User.class);
        List<User> existingUsers = App.session.createQuery(query).getResultList();
        
        boolean userExists = existingUsers.stream()
            .anyMatch(user -> user.getUsername().equals(newUser.getUsername()));
        
        if (userExists) {
          System.out.println("Registration failed: User already exists");
          client.sendToClient(new LoginResponse(false, "Username already exists", null));
        } else {
          // Save new user
          App.session.beginTransaction();
          App.session.save(newUser);
          App.session.flush();
          App.session.getTransaction().commit();
          System.out.println("User registered successfully: " + newUser.getUsername());
          client.sendToClient(new LoginResponse(true, "Registration successful", null));
        }
      } catch (Exception e) {
        System.err.println("Error during user registration: " + e.getMessage());
        e.printStackTrace();
        try {
          client.sendToClient(new LoginResponse(false, "Registration failed: Server error", null));
        } catch (IOException ioException) {
          System.err.println("Failed to send error response: " + ioException.getMessage());
        }
      }
    } else if (msg instanceof LoginRequest) {
      // Handle login request
      LoginRequest loginRequest = (LoginRequest) msg;
      System.out.println("Server received login request for: " + loginRequest.getUsername());
      
      try {
        // Find user in database
        CriteriaBuilder builder = App.session.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        query.from(User.class);
        List<User> users = App.session.createQuery(query).getResultList();
        
        User foundUser = users.stream()
            .filter(user -> user.getUsername().equals(loginRequest.getUsername()) 
                         && user.getPassword().equals(loginRequest.getPassword()))
            .findFirst()
            .orElse(null);
        
        if (foundUser != null) {
          System.out.println("Login successful for: " + foundUser.getUsername());
          client.sendToClient(new LoginResponse(true, "Login successful", foundUser));
        } else {
          System.out.println("Login failed: Invalid credentials");
          client.sendToClient(new LoginResponse(false, "Invalid username or password", null));
        }
      } catch (Exception e) {
        System.err.println("Error during login: " + e.getMessage());
        e.printStackTrace();
        try {
          client.sendToClient(new LoginResponse(false, "Login failed: Server error", null));
        } catch (IOException ioException) {
          System.err.println("Failed to send error response: " + ioException.getMessage());
        }
      }
    } else if (msg instanceof SubscriptionRequest) {
      // Handle subscription request
      SubscriptionRequest subscriptionRequest = (SubscriptionRequest) msg;
      System.out.println("Server received subscription request from user ID: " + subscriptionRequest.getUserId());
      
      try {
        // Find user and update subscription
        User user = App.session.get(User.class, subscriptionRequest.getUserId());
        if (user != null) {
          user.setSubscriptionType(subscriptionRequest.getRequestedSubscriptionType());
          
          App.session.beginTransaction();
          App.session.update(user);
          App.session.flush();
          App.session.getTransaction().commit();
          
          System.out.println("Subscription approved for user: " + user.getUsername());
          client.sendToClient(new SubscriptionResponse(true, "Subscription request approved", user.getSubscriptionType()));
        } else {
          System.out.println("Subscription failed: User not found");
          client.sendToClient(new SubscriptionResponse(false, "User not found", null));
        }
      } catch (Exception e) {
        System.err.println("Error during subscription request: " + e.getMessage());
        e.printStackTrace();
        try {
          client.sendToClient(new SubscriptionResponse(false, "Subscription failed: Server error", null));
        } catch (IOException ioException) {
          System.err.println("Failed to send error response: " + ioException.getMessage());
        }
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
