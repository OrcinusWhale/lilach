package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

/**
 * Request entity for retrieving the list of available stores
 */
public class StoreListRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Default constructor
    public StoreListRequest() {}
    
    @Override
    public String toString() {
        return "StoreListRequest{}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass();
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
