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
import il.cshaifasweng.OCSFMediatorExample.entities.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.Store;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderItem;
import il.cshaifasweng.OCSFMediatorExample.entities.Cart;
import il.cshaifasweng.OCSFMediatorExample.entities.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;

public class App {

    private static SimpleServer server;
    public static Session session;

    public static SessionFactory getSessionFactory(String pass) throws HibernateException {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.password", pass);
        configuration.addAnnotatedClass(Item.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Employee.class);
        configuration.addAnnotatedClass(Store.class);
        configuration.addAnnotatedClass(Order.class);
        configuration.addAnnotatedClass(OrderItem.class);
        configuration.addAnnotatedClass(Cart.class);
        configuration.addAnnotatedClass(CartItem.class);
        configuration.addAnnotatedClass(Complaint.class);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
        return configuration.buildSessionFactory(serviceRegistry);
    }


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Database password:");
        String pass = scanner.nextLine().trim();
        SessionFactory sessionFactory = getSessionFactory(pass);
        session = sessionFactory.openSession();
        // Initialize sample authentication data
        System.out.println("Initializing sample authentication data...");
        DataInitializer.initializeSampleData();
        server = new SimpleServer(3000);
        server.listen();
    }
}
