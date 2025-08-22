package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import il.cshaifasweng.OCSFMediatorExample.entities.User;

public class App {

  private static SimpleServer server;
  public static Session session;

  public static SessionFactory getSessionFactory(String pass) throws HibernateException {
    Configuration configuration = new Configuration();
    configuration.setProperty("hibernate.connection.password", pass);
    configuration.addAnnotatedClass(Item.class);
    configuration.addAnnotatedClass(User.class);
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties())
        .build();
    return configuration.buildSessionFactory(serviceRegistry);
  }

  private static void generateDb() throws Exception {
    // Create images directory if it doesn't exist
    File imagesDir = new File("images");
    if (!imagesDir.exists()) {
      imagesDir.mkdirs();
      System.out.println("Created images directory: " + imagesDir.getAbsolutePath());
    }
    
    List<String> itemNames = Arrays.asList("Orange Blossom", "White celebration", "Spring Celebration",
        "Sunflower Bouquet", "Lovely Bouquet");
    for (String name : itemNames) {
      // Use relative path from server working directory
      File imageFile = new File(imagesDir, name + ".jpg");
      System.out.println("Creating item: " + name + " with image path: " + imageFile.getAbsolutePath());
      Item item = new Item(name, "Bouquet", 1000, imageFile);
      session.save(item);
    }
    session.flush();
  }

  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Database password:");
    String pass = scanner.nextLine().trim();
    try {
      SessionFactory sessionFactory = getSessionFactory(pass);
      session = sessionFactory.openSession();
      // Always generate sample data for demo purposes
      // Check if Items table is empty and generate data
      CriteriaBuilder builder = App.session.getCriteriaBuilder();
      CriteriaQuery<Item> query = builder.createQuery(Item.class);
      query.from(Item.class);
      List<Item> existingItems = App.session.createQuery(query).getResultList();
      
      if (existingItems.isEmpty()) {
        System.out.println("Generating sample flower data...");
        session.beginTransaction();
        generateDb();
        session.getTransaction().commit();
        System.out.println("Sample flower data generated successfully!");
      } else {
        System.out.println("Found " + existingItems.size() + " existing items in database.");
      }
    } catch (Exception exception) {
      if (session != null) {
        session.getTransaction().rollback();
      }
      System.err.println("Whoops, rollback");
      exception.printStackTrace();
    }
    server = new SimpleServer(3000);
    server.listen();
  }
}
