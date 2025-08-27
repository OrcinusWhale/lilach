package il.cshaifasweng.OCSFMediatorExample.client.reports;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.LoginController;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Store;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportsController {

    @FXML private ComboBox<ReportType> typeCombo;
    @FXML private ComboBox<ReportScope> scopeCombo;
    @FXML private ComboBox<Store>       storeCombo;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private CheckBox compareToggle;
    @FXML private DatePicker fromDatePickerB;
    @FXML private DatePicker toDatePickerB;

    // Quarterly UI (A/B rows appear only for QUARTERLY_REVENUE)
    @FXML private HBox quarterRowA;
    @FXML private HBox quarterRowB;
    @FXML private ComboBox<String>  quarterComboA;
    @FXML private ComboBox<Integer> yearComboA;
    @FXML private ComboBox<String>  quarterComboB;
    @FXML private ComboBox<Integer> yearComboB;

    @FXML private Label statusLabel;

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<CompareRow> compareTable;
    @FXML private TableColumn<CompareRow, String>  colLabel;
    @FXML private TableColumn<CompareRow, Number>  colA;
    @FXML private TableColumn<CompareRow, Number>  colB;
    @FXML private TableColumn<CompareRow, Number>  colDelta;
    @FXML private TableColumn<CompareRow, Number>  colDeltaPct;

    private boolean registeredWithBus = false;

    // Simple model for compare table
    public static class CompareRow {
        private final javafx.beans.property.SimpleStringProperty label    = new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleDoubleProperty a        = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.SimpleDoubleProperty b        = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.SimpleDoubleProperty delta    = new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.SimpleDoubleProperty deltaPct = new javafx.beans.property.SimpleDoubleProperty();
        public CompareRow(String label, double av, double bv, double dv, double dp) {
            this.label.set(label); this.a.set(av); this.b.set(bv); this.delta.set(dv); this.deltaPct.set(dp);
        }
        public String getLabel()    { return label.get(); }
        public double getA()        { return a.get(); }
        public double getB()        { return b.get(); }
        public double getDelta()    { return delta.get(); }
        public double getDeltaPct() { return deltaPct.get(); }
    }

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            registeredWithBus = true;
        }

        // Report types
        typeCombo.setItems(FXCollections.observableArrayList(
                ReportType.QUARTERLY_REVENUE,
                ReportType.ORDERS_BY_PRODUCT_TYPE,
                ReportType.COMPLAINTS_BY_DAY
        ));
        typeCombo.getSelectionModel().select(ReportType.QUARTERLY_REVENUE);

        // Scopes
        scopeCombo.setItems(FXCollections.observableArrayList(
                ReportScope.NETWORK,
                ReportScope.BRANCH
        ));
        scopeCombo.getSelectionModel().select(ReportScope.NETWORK);
        scopeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, v) -> {
            boolean branch = (v == ReportScope.BRANCH);
            storeCombo.setDisable(!branch);
            // keep UI in sync if user flips scope while quarterly is selected
            syncTypeUI();
        });

        // Compare behaviour
        compareToggle.selectedProperty().addListener((obs, was, on) -> {
            boolean isQuarterly = typeCombo.getValue() == ReportType.QUARTERLY_REVENUE;

            if (fromDatePickerB != null) fromDatePickerB.setDisable(!on || isQuarterly);
            if (toDatePickerB   != null) toDatePickerB.setDisable(!on || isQuarterly);

            if (compareTable != null) {
                compareTable.setVisible(on);
                compareTable.setManaged(on);
                if (!on) compareTable.setItems(FXCollections.observableArrayList());
            }

            if (quarterRowB != null) {
                quarterRowB.setVisible(on && isQuarterly);
                quarterRowB.setManaged(on && isQuarterly);
            }
            syncTypeUI();
        });

        // Defaults for date mode
        if (fromDatePicker  != null) fromDatePicker.setValue(LocalDate.now().minusDays(7));
        if (toDatePicker    != null) toDatePicker.setValue(LocalDate.now());

        if (fromDatePickerB != null) fromDatePickerB.setValue(LocalDate.now().minusDays(14));
        if (toDatePickerB   != null) toDatePickerB.setValue(LocalDate.now().minusDays(7));

        // Chart axis labels
        if (xAxis != null) xAxis.setLabel("Label");
        if (yAxis != null) yAxis.setLabel("Value");

        // Compare table columns
        if (colLabel != null)    colLabel.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLabel()));
        if (colA != null)        colA.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getA()));
        if (colB != null)        colB.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getB()));
        if (colDelta != null)    colDelta.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getDelta()));
        if (colDeltaPct != null) colDeltaPct.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getDeltaPct()));

        // Quarterly combos
        var quarters = FXCollections.observableArrayList(
                "Q1 (Jan–Mar)", "Q2 (Apr–Jun)", "Q3 (Jul–Sep)", "Q4 (Oct–Dec)"
        );
        if (quarterComboA != null) { quarterComboA.setItems(quarters); quarterComboA.getSelectionModel().selectFirst(); }
        if (quarterComboB != null) { quarterComboB.setItems(quarters); quarterComboB.getSelectionModel().select(1); }

        int currentYear = LocalDate.now().getYear();
        var years = new ArrayList<Integer>();
        for (int y = currentYear; y >= currentYear - 5; y--) years.add(y);
        if (yearComboA != null) { yearComboA.setItems(FXCollections.observableArrayList(years)); yearComboA.getSelectionModel().selectFirst(); }
        if (yearComboB != null) { yearComboB.setItems(FXCollections.observableArrayList(years)); yearComboB.getSelectionModel().selectFirst(); }

        // When the type changes, re-sync the UI (so quarterly rows appear/disappear)
        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> syncTypeUI());

        // Initial UI sync so quarterly controls appear on first load if default is QUARTERLY_REVENUE
        syncTypeUI();

        // Load stores & apply role limits
        requestStoreList();
        applyRoleLimits();
    }

    /** Apply simple permission rules:
     *  - Admin/brand users: can see everything (Network & Branch)
     *  - Store-specific users: forced to Branch for their store
     *  - Others (e.g., customers): screen disabled
     */
    private void applyRoleLimits() {
        User u = LoginController.getCurrentUser();
        if (u == null) return;


        boolean isCustomerLike = (!u.isAdmin() && !u.isBrandUser() && !u.isStoreSpecific());
        if (isCustomerLike) {
            setAllDisabled(true);
            if (statusLabel != null) statusLabel.setText("Access denied: reports are for managers only.");
            return;
        }

        if (u.isStoreSpecific()) {
            if (scopeCombo != null) {
                scopeCombo.getSelectionModel().select(ReportScope.BRANCH);
                scopeCombo.setDisable(true);
            }
            if (storeCombo != null) {
                storeCombo.setDisable(true); // will select the correct store in onStoreListResponse()
            }
        }
    }

    private void setAllDisabled(boolean disable) {
        if (typeCombo != null) typeCombo.setDisable(disable);
        if (scopeCombo != null) scopeCombo.setDisable(disable);
        if (storeCombo != null) storeCombo.setDisable(disable);
        if (fromDatePicker != null) fromDatePicker.setDisable(disable);
        if (toDatePicker != null) toDatePicker.setDisable(disable);
        if (fromDatePickerB != null) fromDatePickerB.setDisable(disable);
        if (toDatePickerB != null) toDatePickerB.setDisable(disable);
        if (compareToggle != null) compareToggle.setDisable(disable);
        if (quarterRowA != null) { quarterRowA.setDisable(disable); quarterRowA.setManaged(!disable); quarterRowA.setVisible(!disable); }
        if (quarterRowB != null) { quarterRowB.setDisable(disable); quarterRowB.setManaged(!disable); quarterRowB.setVisible(!disable); }
    }

    private void syncTypeUI() {
        ReportType rt = typeCombo.getValue();
        boolean quarterly = (rt == ReportType.QUARTERLY_REVENUE);
        boolean compareOn = (compareToggle != null && compareToggle.isSelected());

        if (fromDatePicker != null)  fromDatePicker.setDisable(quarterly);
        if (toDatePicker   != null)  toDatePicker.setDisable(quarterly);
        if (fromDatePickerB != null) fromDatePickerB.setDisable(quarterly && compareOn);
        if (toDatePickerB   != null) toDatePickerB.setDisable(quarterly && compareOn);

        if (quarterRowA != null) { quarterRowA.setVisible(quarterly); quarterRowA.setManaged(quarterly); }
        if (quarterRowB != null) { quarterRowB.setVisible(quarterly && compareOn); quarterRowB.setManaged(quarterly && compareOn); }

        if (yAxis != null) {
            if (rt == ReportType.ORDERS_BY_PRODUCT_TYPE) yAxis.setLabel("Quantity");
            else if (rt == ReportType.COMPLAINTS_BY_DAY) yAxis.setLabel("Complaints");
            else yAxis.setLabel("Revenue (₪)");
        }
    }

    private void requestStoreList() {
        try {
            App.getClient().sendToServer(new il.cshaifasweng.OCSFMediatorExample.entities.StoreListRequest());
            if (statusLabel != null) statusLabel.setText("Loading stores...");
        } catch (IOException e) {
            if (statusLabel != null) statusLabel.setText("Failed to request stores: " + e.getMessage());
        }
    }

    @FXML
    private void handleRunReports() {
        if (statusLabel != null) statusLabel.setText("Running...");
        clearVisuals();

        if (compareToggle != null && compareToggle.isSelected()) {
            sendCompare();
        } else {
            sendSingle();
        }
    }

    private void clearVisuals() {
        if (barChart != null) barChart.getData().clear();
    }

    private ReportRequest buildRequestFromDates(LocalDate from, LocalDate to) {
        ReportRequest req = new ReportRequest();
        req.setType(typeCombo.getValue());
        req.setScope(scopeCombo.getValue());
        req.setFrom(from);
        req.setTo(to);

        if (req.getScope() == ReportScope.BRANCH) {
            Store s = storeCombo.getValue();
            if (s == null) throw new IllegalArgumentException("Please select a store for Branch scope.");
            Long sid = (s.getStoreId() == null) ? null : s.getStoreId().longValue();
            req.setStoreId(sid);
        } else {
            req.setStoreId(null);
        }
        return req;
    }

    private void sendSingle() {
        try {
            ReportType rt = typeCombo.getValue();
            ReportRequest req;
            if (rt == ReportType.QUARTERLY_REVENUE) {
                Integer year = yearComboA.getValue();
                String qLabel = quarterComboA.getValue();
                if (year == null || qLabel == null) throw new IllegalArgumentException("Select quarter and year.");
                LocalDate[] bounds = quarterBounds(year, qLabel);
                req = buildRequestFromDates(bounds[0], bounds[1]);
            } else {
                req = buildRequestFromDates(fromDatePicker.getValue(), toDatePicker.getValue());
            }

            if (yAxis != null) {
                yAxis.setLabel(rt == ReportType.ORDERS_BY_PRODUCT_TYPE ? "Quantity"
                        : (rt == ReportType.QUARTERLY_REVENUE ? "Revenue (₪)" : "Value"));
            }
            App.getClient().sendToServer(new ReportRequestMessage(req));
        } catch (Exception e) {
            if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void sendCompare() {
        try {
            ReportType rt = typeCombo.getValue();
            ReportRequest a, b;
            if (rt == ReportType.QUARTERLY_REVENUE) {
                Integer yearA = yearComboA.getValue();
                String  qA    = quarterComboA.getValue();
                Integer yearB = yearComboB.getValue();
                String  qB    = quarterComboB.getValue();
                if (yearA == null || qA == null || yearB == null || qB == null)
                    throw new IllegalArgumentException("Select quarters and years for both ranges.");
                LocalDate[] A = quarterBounds(yearA, qA);
                LocalDate[] B = quarterBounds(yearB, qB);
                a = buildRequestFromDates(A[0], A[1]);
                b = buildRequestFromDates(B[0], B[1]);
            } else {
                a = buildRequestFromDates(fromDatePicker.getValue(),  toDatePicker.getValue());
                b = buildRequestFromDates(fromDatePickerB.getValue(), toDatePickerB.getValue());
            }
            // enforce same type/scope/store for compare
            b.setType(a.getType());
            b.setScope(a.getScope());
            b.setStoreId(a.getStoreId());

            if (yAxis != null) {
                yAxis.setLabel(rt == ReportType.ORDERS_BY_PRODUCT_TYPE ? "Quantity"
                        : (rt == ReportType.QUARTERLY_REVENUE ? "Revenue (₪)" : "Value"));
            }

            ReportCompareRequest creq = new ReportCompareRequest();
            creq.setA(a);
            creq.setB(b);

            App.getClient().sendToServer(new ReportCompareRequestMessage(creq));
        } catch (Exception e) {
            if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @Subscribe
    public void onReportResponse(ReportResponseMessage msg) {
        Platform.runLater(() -> {
            if (compareToggle != null && compareToggle.isSelected()) return;
            if (!msg.isOk()) { if (statusLabel != null) statusLabel.setText("Error: " + msg.getError()); return; }
            renderSingle(msg.getResponse());
        });
    }

    @Subscribe
    public void onCompareResponse(ReportCompareResponseMessage msg) {
        Platform.runLater(() -> {
            if (compareToggle != null && !compareToggle.isSelected()) return;
            if (!msg.isOk()) { if (statusLabel != null) statusLabel.setText("Error: " + msg.getError()); return; }
            renderCompare(msg.getResponse());
        });
    }

    @Subscribe
    public void onStoreListResponse(il.cshaifasweng.OCSFMediatorExample.entities.StoreListResponse msg) {
        Platform.runLater(() -> {
            List<Store> stores = msg.getStores();
            if (storeCombo != null) storeCombo.setItems(FXCollections.observableArrayList(stores));
            if (statusLabel != null) statusLabel.setText("Stores loaded (" + (stores == null ? 0 : stores.size()) + ").");

            // If user is store-specific, auto-select their store
            User u = LoginController.getCurrentUser();
            if (u != null && u.isStoreSpecific() && u.getStore() != null && stores != null) {
                Integer myId = u.getStore().getStoreId();
                if (myId != null) {
                    for (Store s : stores) {
                        if (myId.equals(s.getStoreId())) {
                            storeCombo.getSelectionModel().select(s);
                            break;
                        }
                    }
                }
            } else {
                // Default: if Branch scope and no selection yet, select first
                if (scopeCombo.getValue() == ReportScope.BRANCH && stores != null && !stores.isEmpty()) {
                    if (storeCombo.getSelectionModel().isEmpty()) {
                        storeCombo.getSelectionModel().selectFirst();
                    }
                }
            }
        });
    }

    private void renderSingle(ReportResponse res) {
        if (res == null) { if (statusLabel != null) statusLabel.setText("No data."); return; }

        final XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ReportDatum d : res.getData()) {
            series.getData().add(new XYChart.Data<>(d.getLabel(), d.getValue()));
        }
        series.setName(typeCombo.getValue() == ReportType.ORDERS_BY_PRODUCT_TYPE ? "Quantity" :
                typeCombo.getValue() == ReportType.COMPLAINTS_BY_DAY ? "Complaints" : "Revenue");

        if (barChart != null) barChart.getData().setAll(series);

        String summary = "Done.";
        if (res.getMeta() != null) {
            Object totalRev = res.getMeta().get("totalRevenue");
            Object totalQty = res.getMeta().get("totalQuantity");
            Object totalCmp = res.getMeta().get("totalComplaints");
            if (totalRev != null) summary = "Done. Total: " + totalRev;
            if (totalQty != null) summary = "Done. Total qty: " + totalQty;
            if (totalCmp != null) summary = "Done. Complaints: " + totalCmp;
        }
        if (statusLabel != null) statusLabel.setText(summary);
    }

    private void renderCompare(ReportCompareResponse res) {
        if (res == null) { if (statusLabel != null) statusLabel.setText("No data."); return; }

        var rows = FXCollections.<CompareRow>observableArrayList();
        for (ReportCompareResponse.CompareDatum d : res.getDiffs()) {
            rows.add(new CompareRow(d.getLabel(), d.getLeftValue(), d.getRightValue(), d.getDelta(), d.getDeltaPct()));
        }
        if (compareTable != null) compareTable.setItems(rows);
        if (statusLabel != null) statusLabel.setText("Done. Compared " + rows.size() + " rows.");

        if (res.getLeft() != null) {
            final XYChart.Series<String, Number> a = new XYChart.Series<>();
            a.setName("Range A");
            for (ReportDatum d : res.getLeft().getData()) a.getData().add(new XYChart.Data<>(d.getLabel(), d.getValue()));

            final XYChart.Series<String, Number> b = new XYChart.Series<>();
            b.setName("Range B");
            if (res.getRight() != null) {
                for (ReportDatum d : res.getRight().getData()) b.getData().add(new XYChart.Data<>(d.getLabel(), d.getValue()));
            }
            if (barChart != null) barChart.getData().setAll(a, b);
        }
    }

    public void onClosed() {
        try {
            if (registeredWithBus) {
                EventBus.getDefault().unregister(this);
                registeredWithBus = false;
            }
        } catch (Exception ignored) { }
    }

    private static LocalDate[] quarterBounds(int year, String quarterLabel) {
        int q = 1;
        if (quarterLabel != null && quarterLabel.length() >= 2 && quarterLabel.charAt(0) == 'Q') {
            char c = quarterLabel.charAt(1);
            if (c >= '1' && c <= '4') q = (c - '0');
        }
        LocalDate start;
        LocalDate end;
        switch (q) {
            case 1: start = LocalDate.of(year, 1, 1);  end = LocalDate.of(year, 3, 31);  break;
            case 2: start = LocalDate.of(year, 4, 1);  end = LocalDate.of(year, 6, 30);  break;
            case 3: start = LocalDate.of(year, 7, 1);  end = LocalDate.of(year, 9, 30);  break;
            case 4:
            default: start = LocalDate.of(year, 10, 1); end = LocalDate.of(year, 12, 31); break;
        }
        return new LocalDate[]{ start, end };
    }

    @SuppressWarnings("unused")
    private SimpleClient client() { return App.getClient(); }
}
