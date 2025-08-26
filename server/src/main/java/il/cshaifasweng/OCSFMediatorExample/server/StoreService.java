package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Store;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListResponse;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Service class for managing store operations
 */
public class StoreService {
    
    private final Session session;
    
    public StoreService(Session session) {
        this.session = session;
    }
    
    /**
     * Get all available stores
     * @return StoreListResponse containing all stores
     */
    public StoreListResponse getAllStores() {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Store> query = builder.createQuery(Store.class);
            query.from(Store.class);
            List<Store> stores = session.createQuery(query).getResultList();
            
            return new StoreListResponse(true, "Stores retrieved successfully", stores);
        } catch (Exception e) {
            e.printStackTrace();
            return new StoreListResponse(false, "Error retrieving stores: " + e.getMessage());
        }
    }
    
    /**
     * Get store by ID
     * @param storeId the store ID
     * @return Store object or null if not found
     */
    public Store getStoreById(Integer storeId) {
        try {
            return session.get(Store.class, storeId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a new store
     * @param store the store to create
     * @return the created store with ID
     */
    public Store createStore(Store store) {
        try {
            session.beginTransaction();
            session.save(store);
            session.getTransaction().commit();
            return store;
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Initialize default stores if none exist
     */
    public void initializeDefaultStores() {
        try {
            // Check if stores already exist
            Query<Long> countQuery = session.createQuery("SELECT COUNT(*) FROM Store", Long.class);
            Long storeCount = countQuery.uniqueResult();
            
            if (storeCount == 0) {
                session.beginTransaction();
                
                // Create default stores
                Store telAvivStore = new Store("Lilach Tel Aviv", "123 Dizengoff Street, Tel Aviv", 
                                             "03-1234567", "telaviv@lilach.com", "Sarah Cohen");
                Store jerusalemStore = new Store("Lilach Jerusalem", "456 King George Street, Jerusalem", 
                                                "02-7654321", "jerusalem@lilach.com", "David Levi");
                Store haifaStore = new Store("Lilach Haifa", "789 Herzl Street, Haifa", 
                                           "04-9876543", "haifa@lilach.com", "Rachel Green");
                
                session.save(telAvivStore);
                session.save(jerusalemStore);
                session.save(haifaStore);
                
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            System.err.println("Error initializing default stores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update an existing store
     * @param store the store to update
     * @return the updated store
     */
    public Store updateStore(Store store) {
        try {
            session.beginTransaction();
            session.update(store);
            session.getTransaction().commit();
            return store;
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Delete a store by ID
     * @param storeId the store ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteStore(Integer storeId) {
        try {
            Store store = session.get(Store.class, storeId);
            if (store != null) {
                session.beginTransaction();
                session.delete(store);
                session.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
}
