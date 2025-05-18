package il.cshaifasweng.OCSFMediatorExample.server;

import java.util.Scanner;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import java.time.LocalDateTime;
import java.util.Scanner;

public class App {

  private static SimpleServer server;
  public static Session session;

  public static SessionFactory getSessionFactory(String pass) throws HibernateException {
    Configuration configuration = new Configuration();
    configuration.setProperty("hibernate.connection.password", pass);
    configuration.addAnnotatedClass(Item.class);
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .applySettings(configuration.getProperties())
        .build();
    return configuration.buildSessionFactory(serviceRegistry);
  }

  private static void generateDb() throws Exception {
    List<String> itemNames = Arrays.asList("Floral Embrace", "Lovely Lavender Medley", "Mother’s Embrace",
        "Vibrant Floral Medley", "Precious Peony Bouquet");
    for (String name : itemNames) {
      Item item = new Item(name, "Bouquet", 1000);
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
      CriteriaBuilder builder = App.session.getCriteriaBuilder();
      CriteriaQuery<Item> query = builder.createQuery(Item.class);
      query.from(Item.class);
      if (App.session.createQuery(query).getResultList().isEmpty()) {
        session.beginTransaction();
        generateDb();
        session.getTransaction().commit();
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
