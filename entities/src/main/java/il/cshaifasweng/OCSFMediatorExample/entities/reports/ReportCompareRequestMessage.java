package il.cshaifasweng.OCSFMediatorExample.entities.reports;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import java.io.Serializable;

public class ReportCompareRequestMessage implements Serializable {
    private ReportCompareRequest request;
    private User user;

    public ReportCompareRequestMessage() {}
    public ReportCompareRequestMessage(ReportCompareRequest request) { this.request = request; }
    public ReportCompareRequestMessage(ReportCompareRequest request, User user) { 
        this.request = request; 
        this.user = user;
    }

    public ReportCompareRequest getRequest() { return request; }
    public void setRequest(ReportCompareRequest request) { this.request = request; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
