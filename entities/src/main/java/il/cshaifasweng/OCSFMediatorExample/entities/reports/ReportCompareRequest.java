package il.cshaifasweng.OCSFMediatorExample.entities.reports;

import java.io.Serializable;

public class ReportCompareRequest implements Serializable {
    private ReportRequest a;  // first range
    private ReportRequest b;  // second range

    public ReportRequest getA() { return a; }
    public void setA(ReportRequest a) { this.a = a; }

    public ReportRequest getB() { return b; }
    public void setB(ReportRequest b) { this.b = b; }
}
