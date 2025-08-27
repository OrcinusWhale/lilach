package il.cshaifasweng.OCSFMediatorExample.entities.reports;

import java.io.Serializable;

public class ReportCompareRequestMessage implements Serializable {
    private ReportCompareRequest request;

    public ReportCompareRequestMessage() {}
    public ReportCompareRequestMessage(ReportCompareRequest request) { this.request = request; }

    public ReportCompareRequest getRequest() { return request; }
    public void setRequest(ReportCompareRequest request) { this.request = request; }
}
