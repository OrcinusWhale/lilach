package il.cshaifasweng.OCSFMediatorExample.entities.reports;
import java.io.Serializable;

public class ReportRequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ReportRequest request;

    public ReportRequestMessage() {}                       // no-args (for serialization)
    public ReportRequestMessage(ReportRequest request) {   // convenience ctor
        this.request = request;
    }

    public ReportRequest getRequest() { return request; }  // <-- needed by server
    public void setRequest(ReportRequest request) { this.request = request; }
}
