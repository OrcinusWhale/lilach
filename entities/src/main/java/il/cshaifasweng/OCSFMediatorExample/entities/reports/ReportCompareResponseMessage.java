package il.cshaifasweng.OCSFMediatorExample.entities.reports;

import java.io.Serializable;

public class ReportCompareResponseMessage implements Serializable {
    private boolean ok;
    private ReportCompareResponse response;
    private String error;

    public ReportCompareResponseMessage() {}
    public ReportCompareResponseMessage(boolean ok, ReportCompareResponse response, String error) {
        this.ok = ok; this.response = response; this.error = error;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public ReportCompareResponse getResponse() { return response; }
    public void setResponse(ReportCompareResponse response) { this.response = response; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
