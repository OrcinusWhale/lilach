package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "stores")
public class Store implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer storeId;
    
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;
    
    @Column(name = "address", length = 255)
    private String address;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "manager_name", length = 100)
    private String managerName;
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();
    
    @ManyToMany(mappedBy = "stores")
    private Set<Item> items = new HashSet<>();

    // Default constructor
    public Store() {}
    
    // Constructor with required fields
    public Store(String storeName, String address) {
        this.storeName = storeName;
        this.address = address;
    }
    
    // Constructor with all fields
    public Store(String storeName, String address, String phone, String email, String managerName) {
        this.storeName = storeName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.managerName = managerName;
    }
    
    // Getters and Setters
    public Integer getStoreId() {
        return storeId;
    }
    
    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getManagerName() {
        return managerName;
    }
    
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    // Helper methods
    public void addUser(User user) {
        users.add(user);
        user.setStore(this);
    }
    
    public void removeUser(User user) {
        users.remove(user);
        user.setStore(null);
    }
    
    public void addItem(Item item) {
        if (item == null) return;
        items.add(item);
        if (!item.getStores().contains(this)) {
            item.getStores().add(this);
        }
    }

    public void removeItem(Item item) {
        if (item == null) return;
        items.remove(item);
        item.getStores().remove(this);
    }

    @Override
    public String toString() {
        return storeName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Store store = (Store) obj;
        return storeId != null && storeId.equals(store.storeId);
    }
    
    @Override
    public int hashCode() {
        return storeId != null ? storeId.hashCode() : 0;
    }
}
