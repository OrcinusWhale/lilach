package il.cshaifasweng.OCSFMediatorExample.entities.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportCompareResponse implements Serializable {
    private ReportResponse left;   // response for A
    private ReportResponse right;  // response for B
    private List<CompareDatum> diffs = new ArrayList<>();

    public ReportResponse getLeft() { return left; }
    public void setLeft(ReportResponse left) { this.left = left; }

    public ReportResponse getRight() { return right; }
    public void setRight(ReportResponse right) { this.right = right; }

    public List<CompareDatum> getDiffs() { return diffs; }
    public void setDiffs(List<CompareDatum> diffs) { this.diffs = diffs; }

    public static class CompareDatum implements Serializable {
        private String label;
        private double leftValue;
        private double rightValue;
        private double delta;     // right - left
        private double deltaPct;  // in %

        public CompareDatum() {}

        public CompareDatum(String label, double leftValue, double rightValue) {
            this.label = label;
            this.leftValue = leftValue;
            this.rightValue = rightValue;
            this.delta = rightValue - leftValue;
            this.deltaPct = (leftValue == 0.0) ? 0.0 : (this.delta / leftValue) * 100.0;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public double getLeftValue() { return leftValue; }
        public void setLeftValue(double leftValue) { this.leftValue = leftValue; }

        public double getRightValue() { return rightValue; }
        public void setRightValue(double rightValue) { this.rightValue = rightValue; }

        public double getDelta() { return delta; }
        public void setDelta(double delta) { this.delta = delta; }

        public double getDeltaPct() { return deltaPct; }
        public void setDeltaPct(double deltaPct) { this.deltaPct = deltaPct; }
    }
}
