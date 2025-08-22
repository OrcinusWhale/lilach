package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Response entity for store list requests
 */
public class StoreListResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private List<Store> stores;
    
    // Default constructor
    public StoreListResponse() {
        this.stores = new ArrayList<>();
    }
    
    // Constructor with success status
    public StoreListResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.stores = new ArrayList<>();
    }
    
    // Constructor with stores list
    public StoreListResponse(boolean success, String message, List<Store> stores) {
        this.success = success;
        this.message = message;
        this.stores = stores != null ? stores : new ArrayList<>();
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<Store> getStores() {
        return stores;
    }
    
    public void setStores(List<Store> stores) {
        this.stores = stores != null ? stores : new ArrayList<>();
    }
}
