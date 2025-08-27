package il.cshaifasweng.OCSFMediatorExample.entities.reports;
import java.io.Serializable;

public class ReportResponseMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean ok;
    private String error;
    private ReportResponse response;

    public ReportResponseMessage() {}  // no-args

    // convenience ctor used by the server
    public ReportResponseMessage(boolean ok, ReportResponse response, String error) {
        this.ok = ok;
        this.response = response;
        this.error = error;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public ReportResponse getResponse() { return response; }
    public void setResponse(ReportResponse response) { this.response = response; }
}
