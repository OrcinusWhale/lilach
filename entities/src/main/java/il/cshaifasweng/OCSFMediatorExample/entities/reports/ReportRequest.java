package il.cshaifasweng.OCSFMediatorExample.entities.reports;
import java.io.Serializable; import java.time.LocalDate;
public class ReportRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private ReportType type; private ReportScope scope; private Long storeId;
    private LocalDate from; private LocalDate to; // inclusive
    private Integer year; private Integer quarter; // optional (for later)
    public ReportType getType(){return type;} public void setType(ReportType t){this.type=t;}
    public ReportScope getScope(){return scope;} public void setScope(ReportScope s){this.scope=s;}
    public Long getStoreId(){return storeId;} public void setStoreId(Long v){this.storeId=v;}
    public LocalDate getFrom(){return from;} public void setFrom(LocalDate v){this.from=v;}
    public LocalDate getTo(){return to;} public void setTo(LocalDate v){this.to=v;}
    public Integer getYear(){return year;} public void setYear(Integer v){this.year=v;}
    public Integer getQuarter(){return quarter;} public void setQuarter(Integer v){this.quarter=v;}
}
