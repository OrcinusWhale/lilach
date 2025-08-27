package il.cshaifasweng.OCSFMediatorExample.entities.reports;
import java.io.Serializable;
public class ReportDatum implements Serializable {
    private static final long serialVersionUID = 1L;
    public ReportDatum() {}
    public ReportDatum(String label, double value) { this.label = label; this.value = value; }
    private String label; private double value;
    public String getLabel() { return label; } public void setLabel(String label) { this.label = label; }
    public double getValue() { return value; } public void setValue(double value) { this.value = value; }
}
