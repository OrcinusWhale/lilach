package il.cshaifasweng.OCSFMediatorExample.entities.reports;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import java.io.Serializable;

public class ReportRequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ReportRequest request;
    private User user;

    public ReportRequestMessage() {}                       // no-args (for serialization)
    public ReportRequestMessage(ReportRequest request) {   // convenience ctor
        this.request = request;
    }
    
    public ReportRequestMessage(ReportRequest request, User user) {   // convenience ctor with user
        this.request = request;
        this.user = user;
    }

    public ReportRequest getRequest() { return request; }  // <-- needed by server
    public void setRequest(ReportRequest request) { this.request = request; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
