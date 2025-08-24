package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.AddResponseEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.CatalogueEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.NewItemEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.UpdateItemEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.SubscriptionRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.SubscriptionResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.EmployeeManagementRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.EmployeeManagementResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.AccountSetupResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.UserSubscriptionSetupRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.StoreListResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.CartResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.CreateOrderRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderHistoryRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderHistoryResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderCancellationRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderCancellationResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderDetailsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.OrderDetailsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.SessionRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.SessionResponse;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Item;
import il.cshaifasweng.OCSFMediatorExample.entities.Store;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.HibernateException;

public class SimpleServer extends AbstractServer {
    private static final ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
    private SubscriptionService subscriptionService;
    private StoreService storeService;
    private OrderService orderService;

    public SimpleServer(int port) {
        super(port);
        // Initialize SubscriptionService with session instead of sessionFactory
        this.subscriptionService = new SubscriptionService(App.session.getSessionFactory());
        // Initialize StoreService
        this.storeService = new StoreService(App.session);
        // Initialize default stores
        this.storeService.initializeDefaultStores();
        // Initialize OrderService
        this.orderService = new OrderService(App.session.getSessionFactory());
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Server received message: " + msg + " (Type: " + msg.getClass().getSimpleName() + ")");
        if (msg instanceof String) {
            String msgString = (String) msg;
            System.out.println("Processing string message: '" + msgString + "'");
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
            } else if (msgString.startsWith("LOGOUT_USER:")) {
                // Handle logout message - clean up server-side session
                System.out.println("=== LOGOUT DEBUG: Received logout message: " + msgString + " ===");
                try {
                    String userIdStr = msgString.substring("LOGOUT_USER:".length());
                    int userId = Integer.parseInt(userIdStr);
                    System.out.println("=== LOGOUT DEBUG: Parsed userId: " + userId + " ===");
                    System.out.println("=== LOGOUT DEBUG: Active sessions before cleanup: " + SessionManager.getActiveSessionCount() + " ===");
                    boolean sessionCleaned = SessionManager.terminateSession(userId);
                    System.out.println("=== LOGOUT DEBUG: Session cleanup result: " + sessionCleaned + " ===");
                    System.out.println("=== LOGOUT DEBUG: Active sessions after cleanup: " + SessionManager.getActiveSessionCount() + " ===");
                } catch (NumberFormatException e) {
                    System.err.println("=== LOGOUT DEBUG: Invalid logout message format: " + msgString + " ===");
                }
            } else if (msgString.startsWith("get ") && !msgString.startsWith("getCart ")) {
                // Handle get item request (but not getCart)
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
                SubscribedClient subscribedClient = new SubscribedClient(client);
                if (!SubscribersList.contains(subscribedClient)) {
                    SubscribersList.add(subscribedClient);
                }
            } else if (msgString.equals("remove")) {
                SubscribersList.removeIf(subscribedClient -> subscribedClient.getClient().equals(client));
            } else if (msgString.startsWith("getCart ")) {
                // Handle get cart request
                System.out.println("Processing getCart string: '" + msgString + "'");
                int userId = Integer.parseInt(msgString.split(" ")[1]);
                System.out.println("Server received get cart request for user ID: " + userId);

                try {
                    CartResponse response = orderService.getCart(userId);
                    System.out.println("Server sending cart response - Success: " + response.isSuccess() +
                            ", Items: " + (response.getCartItems() != null ? response.getCartItems().size() : 0));
                    client.sendToClient(response);
                } catch (IOException e) {
                    System.err.println("Failed to send cart response: " + e.getMessage());
                }
            } else if (msgString.startsWith("updateCart ")) {
                // Handle update cart item quantity request
                String[] parts = msgString.split(" ");
                int userId = Integer.parseInt(parts[1]);
                int itemId = Integer.parseInt(parts[2]);
                int quantity = Integer.parseInt(parts[3]);
                System.out.println("Server received update cart request for user ID: " + userId);

                try {
                    CartResponse response = orderService.updateCartItemQuantity(userId, itemId, quantity);
                    client.sendToClient(response);
                } catch (IOException e) {
                    System.err.println("Failed to send cart response: " + e.getMessage());
                }
            } else if (msgString.startsWith("clearCart ")) {
                // Handle clear cart request
                int userId = Integer.parseInt(msgString.split(" ")[1]);
                System.out.println("Server received clear cart request for user ID: " + userId);

                try {
                    CartResponse response = orderService.clearCart(userId);
                    client.sendToClient(response);
                } catch (IOException e) {
                    System.err.println("Failed to send cart response: " + e.getMessage());
                }
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
            // Handle store associations: clear and reattach managed stores
            try {
                App.session.beginTransaction();
                dbItem.getStores().clear();
                if (item.getStores() != null) {
                    for (Store st : item.getStores()) {
                        if (st != null && st.getStoreId() != null) {
                            Store managed = App.session.get(Store.class, st.getStoreId());
                            if (managed != null) {
                                dbItem.addStore(managed);
                            } else {
                                System.err.println("Store id " + st.getStoreId() + " not found while adding item");
                            }
                        }
                    }
                }
                App.session.saveOrUpdate(dbItem);
                App.session.flush();
                App.session.getTransaction().commit();
            } catch (Exception e) {
                if (App.session.getTransaction().isActive()) {
                    App.session.getTransaction().rollback();
                }
                e.printStackTrace();
                return;
            }
            dbItem.loadImage();
            if (newItem) {
                sendToAllClients(new NewItemEvent(dbItem));
            } else {
                sendToAllClients(new UpdateItemEvent(dbItem));
            }
            try {
                client.sendToClient(new AddResponseEvent("add success"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (msg instanceof User) {
            // Handle user registration using AuthenticationService
            User newUser = (User) msg;
            System.out.println("Server received user registration request for: " + newUser.getUsername());

            try {
                LoginResponse response = AuthenticationService.registerUser(newUser);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send registration response: " + e.getMessage());
            }
        } else if (msg instanceof LoginRequest) {
            // Handle login request using AuthenticationService
            LoginRequest loginRequest = (LoginRequest) msg;
            System.out.println("Server received login request for: " + loginRequest.getUsername());

            try {
                LoginResponse response = AuthenticationService.authenticateUser(loginRequest, client);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send login response: " + e.getMessage());
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
        } else if (msg instanceof EmployeeManagementRequest) {
            // Handle employee management requests
            EmployeeManagementRequest empRequest = (EmployeeManagementRequest) msg;
            System.out.println("Server received employee management request: " + empRequest.getRequestType());

            try {
                EmployeeManagementResponse response = handleEmployeeManagement(empRequest);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send employee management response: " + e.getMessage());
            }
        } else if (msg instanceof AccountSetupRequest) {
            // Handle account setup request for annual subscription
            AccountSetupRequest setupRequest = (AccountSetupRequest) msg;
            System.out.println("Server received account setup request for: " + setupRequest.getUsername());

            try {
                AccountSetupResponse response = subscriptionService.setupAccount(setupRequest);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send account setup response: " + e.getMessage());
            }
        } else if (msg instanceof StoreListRequest) {
            // Handle store list request
            System.out.println("Server received store list request");
            try {
                StoreListResponse response = storeService.getAllStores();
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send store list response: " + e.getMessage());
            }
        } else if (msg instanceof UserSubscriptionSetupRequest) {
            // Handle subscription setup request for existing logged-in users
            UserSubscriptionSetupRequest setupRequest = (UserSubscriptionSetupRequest) msg;
            System.out.println("Server received subscription setup request for user ID: " + setupRequest.getUserId());
            System.out.println("Request details: " + setupRequest.getTaxRegistrationNumber() + ", " + setupRequest.getCustomerId());

            try {
                AccountSetupResponse response = subscriptionService.setupSubscriptionForExistingUser(setupRequest);
                System.out.println("Subscription service returned response: " + response.isSuccess() + " - " + response.getMessage());
                client.sendToClient(response);
                System.out.println("Response sent to client successfully");
            } catch (Exception e) {
                System.err.println("Error in subscription setup: " + e.getMessage());
                e.printStackTrace();
                try {
                    AccountSetupResponse errorResponse = new AccountSetupResponse(false, "Server error: " + e.getMessage());
                    client.sendToClient(errorResponse);
                } catch (IOException ioE) {
                    System.err.println("Failed to send error response: " + ioE.getMessage());
                }
            }
        } else if (msg instanceof AddToCartRequest) {
            // Handle add to cart request
            AddToCartRequest cartRequest = (AddToCartRequest) msg;
            System.out.println("Server received add to cart request for user ID: " + cartRequest.getUserId());

            try {
                CartResponse response = orderService.addToCart(cartRequest);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send cart response: " + e.getMessage());
            }
            // Cart handlers moved inside the String block above
        } else if (msg instanceof CreateOrderRequest) {
            // Handle create order request
            CreateOrderRequest orderRequest = (CreateOrderRequest) msg;
            System.out.println("Server received create order request for user ID: " + orderRequest.getUserId());

            try {
                OrderResponse response = orderService.createOrder(orderRequest);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send order response: " + e.getMessage());
            }
        } else if (msg instanceof OrderHistoryRequest) {
            // Handle order history request
            OrderHistoryRequest historyRequest = (OrderHistoryRequest) msg;
            System.out.println("Server received order history request for user ID: " + historyRequest.getUserId());

            try {
                OrderHistoryResponse response = orderService.getUserOrderHistory(historyRequest.getUserId());
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send order history response: " + e.getMessage());
            }
        } else if (msg instanceof OrderCancellationRequest) {
            // Handle order cancellation request
            OrderCancellationRequest cancellationRequest = (OrderCancellationRequest) msg;
            System.out.println("Server received order cancellation request for order ID: " + cancellationRequest.getOrderId() + " from user ID: " + cancellationRequest.getUserId());

            try {
                OrderCancellationResponse response = orderService.cancelOrder(cancellationRequest);
                System.out.println("Order cancellation processed - Success: " + response.isSuccess() + ", Refund: $" + response.getRefundAmount());
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send order cancellation response: " + e.getMessage());
            }
        } else if (msg instanceof OrderDetailsRequest) {
            // Handle order details request
            OrderDetailsRequest detailsRequest = (OrderDetailsRequest) msg;
            System.out.println("Server received order details request for order ID: " + detailsRequest.getOrderId() + " from user ID: " + detailsRequest.getUserId());

            try {
                OrderDetailsResponse response = orderService.getOrderDetails(detailsRequest);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send order details response: " + e.getMessage());
            }
        } else if (msg instanceof SessionRequest) {
            // Handle session management requests
            SessionRequest sessionRequest = (SessionRequest) msg;
            System.out.println("Server received session request: " + sessionRequest.getRequestType() + " for user ID: " + sessionRequest.getUserId());

            try {
                SessionResponse response = handleSessionRequest(sessionRequest);
                client.sendToClient(response);
            } catch (IOException e) {
                System.err.println("Failed to send session response: " + e.getMessage());
            }
        }
    }

    private SessionResponse handleSessionRequest(SessionRequest request) {
        try {
            switch (request.getRequestType()) {
                case SessionRequest.RequestType.LOGOUT:
                    boolean loggedOut = AuthenticationService.logoutUser(request.getUserId());
                    if (loggedOut) {
                        System.out.println("User " + request.getUserId() + " logged out successfully");
                        return new SessionResponse(true, "Logout successful");
                    } else {
                        System.out.println("Logout failed for user " + request.getUserId() + " - no active session found");
                        return new SessionResponse(false, "No active session found");
                    }

                case SessionRequest.RequestType.VALIDATE_SESSION:
                    boolean valid = AuthenticationService.validateSession(request.getSessionId(), request.getUserId());
                    if (valid) {
                        return new SessionResponse(true, "Session is valid");
                    } else {
                        return new SessionResponse(false, "Session is invalid or expired");
                    }

                default:
                    return new SessionResponse(false, "Unknown session request type");
            }
        } catch (Exception e) {
            System.err.println("Error handling session request: " + e.getMessage());
            e.printStackTrace();
            return new SessionResponse(false, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client disconnected: " + client.toString());

        // Clean up any active sessions for this connection
        boolean sessionCleaned = AuthenticationService.cleanupSession(client);
        if (sessionCleaned) {
            System.out.println("Session cleaned up for disconnected client");
        }

        // Call parent implementation
        super.clientDisconnected(client);
    }

    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        System.out.println("Client exception for: " + client.toString() + " - " + exception.getMessage());

        // Clean up any active sessions for this connection
        boolean sessionCleaned = AuthenticationService.cleanupSession(client);
        if (sessionCleaned) {
            System.out.println("Session cleaned up for client with exception");
        }

        // Call parent implementation
        super.clientException(client, exception);
    }

    private EmployeeManagementResponse handleEmployeeManagement(EmployeeManagementRequest request) {
        // Check if user has admin privileges
        if (!AuthenticationService.canEditEmployeeDetails(request.getAdminUser())) {
            return new EmployeeManagementResponse(false, "Access denied: Admin privileges required");
        }

        try {
            switch (request.getRequestType()) {
                case GET_ALL_EMPLOYEES:
                    return getAllEmployees();

                case GET_EMPLOYEE_BY_ID:
                    return getEmployeeById(request.getEmployeeId());

                case CREATE_EMPLOYEE:
                    return createEmployee(request.getEmployeeData());

                case UPDATE_EMPLOYEE:
                    return updateEmployee(request.getEmployeeData());

                case DELETE_EMPLOYEE:
                    return deleteEmployee(request.getEmployeeId());

                default:
                    return new EmployeeManagementResponse(false, "Unknown request type");
            }
        } catch (Exception e) {
            System.err.println("Error handling employee management request: " + e.getMessage());
            e.printStackTrace();
            return new EmployeeManagementResponse(false, "Server error: " + e.getMessage());
        }
    }

    private EmployeeManagementResponse getAllEmployees() {
        try {
            CriteriaBuilder builder = App.session.getCriteriaBuilder();
            CriteriaQuery<Employee> query = builder.createQuery(Employee.class);
            query.from(Employee.class);
            List<Employee> employees = App.session.createQuery(query).getResultList();

            return new EmployeeManagementResponse(true, "Employees retrieved successfully", employees);
        } catch (Exception e) {
            return new EmployeeManagementResponse(false, "Failed to retrieve employees: " + e.getMessage());
        }
    }

    private EmployeeManagementResponse getEmployeeById(int employeeId) {
        try {
            Employee employee = App.session.get(Employee.class, employeeId);
            if (employee != null) {
                return new EmployeeManagementResponse(true, "Employee found", employee);
            } else {
                return new EmployeeManagementResponse(false, "Employee not found");
            }
        } catch (Exception e) {
            return new EmployeeManagementResponse(false, "Failed to retrieve employee: " + e.getMessage());
        }
    }

    private EmployeeManagementResponse createEmployee(Employee employeeData) {
        try {
            // Register employee using AuthenticationService
            boolean success = AuthenticationService.registerEmployee(employeeData.getUser(), employeeData);

            if (success) {
                return new EmployeeManagementResponse(true, "Employee created successfully", employeeData);
            } else {
                return new EmployeeManagementResponse(false, "Failed to create employee");
            }
        } catch (Exception e) {
            return new EmployeeManagementResponse(false, "Failed to create employee: " + e.getMessage());
        }
    }

    private EmployeeManagementResponse updateEmployee(Employee employeeData) {
        try {
            App.session.beginTransaction();

            // Update employee data while preserving operational records
            Employee existingEmployee = App.session.get(Employee.class, employeeData.getEmployeeId());
            if (existingEmployee == null) {
                App.session.getTransaction().rollback();
                return new EmployeeManagementResponse(false, "Employee not found");
            }

            // Update basic employee information
            existingEmployee.setDepartment(employeeData.getDepartment());
            existingEmployee.setPosition(employeeData.getPosition());
            existingEmployee.setSalary(employeeData.getSalary());
            existingEmployee.setStatus(employeeData.getStatus());
            existingEmployee.setManagerId(employeeData.getManagerId());

            // Update user information (preserve operational records)
            User existingUser = existingEmployee.getUser();
            User updatedUser = employeeData.getUser();

            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setAddress(updatedUser.getAddress());

            // Don't update username, password, or operational records

            App.session.update(existingEmployee);
            App.session.update(existingUser);
            App.session.flush();
            App.session.getTransaction().commit();

            return new EmployeeManagementResponse(true, "Employee updated successfully", existingEmployee);
        } catch (Exception e) {
            if (App.session.getTransaction().isActive()) {
                App.session.getTransaction().rollback();
            }
            return new EmployeeManagementResponse(false, "Failed to update employee: " + e.getMessage());
        }
    }

    private EmployeeManagementResponse deleteEmployee(int employeeId) {
        try {
            App.session.beginTransaction();

            Employee employee = App.session.get(Employee.class, employeeId);
            if (employee == null) {
                App.session.getTransaction().rollback();
                return new EmployeeManagementResponse(false, "Employee not found");
            }

            // Set employee status to terminated instead of deleting to preserve records
            employee.setStatus(Employee.EmployeeStatus.TERMINATED);
            employee.getUser().setUserType(User.UserType.CUSTOMER); // Revoke employee access

            App.session.update(employee);
            App.session.update(employee.getUser());
            App.session.flush();
            App.session.getTransaction().commit();

            return new EmployeeManagementResponse(true, "Employee terminated successfully");
        } catch (Exception e) {
            if (App.session.getTransaction().isActive()) {
                App.session.getTransaction().rollback();
            }
            return new EmployeeManagementResponse(false, "Failed to terminate employee: " + e.getMessage());
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
