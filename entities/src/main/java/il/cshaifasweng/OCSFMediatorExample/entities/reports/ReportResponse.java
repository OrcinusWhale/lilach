package il.cshaifasweng.OCSFMediatorExample.entities.reports;
import java.io.Serializable; import java.util.*;
public class ReportResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private ReportRequest request;
    private List<ReportDatum> data = new ArrayList<>();
    private Map<String,Object> meta = new HashMap<>();
    public ReportRequest getRequest(){return request;} public void setRequest(ReportRequest r){this.request=r;}
    public List<ReportDatum> getData(){return data;} public void setData(List<ReportDatum> d){this.data=d;}
    public Map<String,Object> getMeta(){return meta;} public void setMeta(Map<String,Object> m){this.meta=m;}
}
