package il.cshaifasweng.OCSFMediatorExample.server.reports;

import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportCompareRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportCompareResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportDatum;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportScope;
import il.cshaifasweng.OCSFMediatorExample.entities.reports.ReportType;
import org.hibernate.Session;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class ReportService {
    private final Session session;

    public ReportService(Session session) { this.session = session; }

    /** Dispatcher: choose which report to run */
    public ReportResponse run(ReportRequest req, User currentUser) {
        if (req == null) throw new IllegalArgumentException("ReportRequest is null");
        if (req.getFrom() == null || req.getTo() == null)
            throw new IllegalArgumentException("from/to dates are required");

        switch (req.getType()) {
            case ORDERS_BY_PRODUCT_TYPE:
                return ordersByProductType(req);
            case COMPLAINTS_BY_DAY:
                return complaintsByDay(req);
            case QUARTERLY_REVENUE:
            default:
                return revenueByDateRange(req);
        }
    }

    private LocalDateTime startOfDay(LocalDate d) { return d.atStartOfDay(); }
    private LocalDateTime endExclusive(LocalDate d) { return d.plusDays(1).atStartOfDay(); }

    /** Revenue over range:
     *  NETWORK: per-store rows
     *  BRANCH:  single row for selected store
     */
    private ReportResponse revenueByDateRange(ReportRequest req) {
        ReportResponse out = new ReportResponse();
        out.setRequest(req);

        LocalDateTime start = startOfDay(req.getFrom());
        LocalDateTime end = endExclusive(req.getTo());

        if (req.getScope() == ReportScope.NETWORK) {
            List<Object[]> rows = session.createQuery(
                            "select s.storeName, sum(o.finalAmount) " +
                                    "from Order o join o.store s " +
                                    "where o.orderDate >= :start and o.orderDate < :end " +
                                    "group by s.storeName order by s.storeName",
                            Object[].class
                    ).setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();

            double total = 0.0;
            for (Object[] r : rows) {
                String storeName = (String) r[0];
                double sum = r[1] == null ? 0.0 : ((Number) r[1]).doubleValue();
                out.getData().add(new ReportDatum(storeName, sum));
                total += sum;
            }
            out.getMeta().put("totalRevenue", total);
            out.getMeta().put("breakdown", "per_store");
        } else {
            if (req.getStoreId() == null)
                throw new IllegalArgumentException("BRANCH scope requires storeId");

            List<Object[]> rows = session.createQuery(
                            "select s.storeName, sum(o.finalAmount) " +
                                    "from Order o join o.store s " +
                                    "where s.storeId = :sid and o.orderDate >= :start and o.orderDate < :end " +
                                    "group by s.storeName",
                            Object[].class
                    ).setParameter("sid", req.getStoreId().intValue())  // Store.storeId is Integer
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();

            double sum = rows.isEmpty() || rows.get(0)[1] == null ? 0.0 : ((Number) rows.get(0)[1]).doubleValue();
            String label = rows.isEmpty() ? ("Store " + req.getStoreId()) : (String) rows.get(0)[0];
            out.getData().add(new ReportDatum(label, sum));
            out.getMeta().put("totalRevenue", sum);
            out.getMeta().put("breakdown", "single_store");
        }
        return out;
    }

    /** Orders by product type over range:
     *  sums OrderItem.quantity grouped by Item.type
     */
    private ReportResponse ordersByProductType(ReportRequest req) {
        ReportResponse out = new ReportResponse();
        out.setRequest(req);

        LocalDateTime start = startOfDay(req.getFrom());
        LocalDateTime end = endExclusive(req.getTo());

        String jpql =
                "select i.type, sum(oi.quantity) " +
                        "from OrderItem oi join oi.item i join oi.order o " +
                        (req.getScope() == ReportScope.BRANCH ? "join o.store s " : "") +
                        "where o.orderDate >= :start and o.orderDate < :end " +
                        (req.getScope() == ReportScope.BRANCH ? "and s.storeId = :sid " : "") +
                        "group by i.type order by i.type";

        var q = session.createQuery(jpql, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end);
        if (req.getScope() == ReportScope.BRANCH) {
            q.setParameter("sid", req.getStoreId().intValue()); // Store.storeId is Integer
        }

        List<Object[]> rows = q.getResultList();

        double total = 0.0;
        for (Object[] r : rows) {
            String type = (String) r[0];
            double qty = r[1] == null ? 0.0 : ((Number) r[1]).doubleValue();
            out.getData().add(new ReportDatum(type, qty));
            total += qty;
        }
        out.getMeta().put("totalQuantity", total);
        out.getMeta().put("breakdown", req.getScope() == ReportScope.BRANCH ? "single_store" : "network");
        return out;
    }



    private ReportResponse complaintsByDay(ReportRequest req) {
        if (req.getFrom() == null || req.getTo() == null) {
            throw new IllegalArgumentException("from/to dates are required");
        }
        boolean branch = (req.getScope() == ReportScope.BRANCH);
        if (branch && req.getStoreId() == null) {
            throw new IllegalArgumentException("BRANCH scope requires storeId");
        }

        ReportResponse out = new ReportResponse();
        out.setRequest(req);

        // Build Instant bounds (NOT LocalDateTime) because Complaint.createdAt is Instant
        java.time.ZoneId tz = java.time.ZoneId.systemDefault();
        java.time.Instant startI = req.getFrom().atStartOfDay(tz).toInstant();
        java.time.Instant endI   = req.getTo().plusDays(1).atStartOfDay(tz).toInstant();

        final String jpql;
        if (branch) {
            // Link complaints to orders by orderNumber == str(orderId), then to store
            jpql =
                    "select function('date', c.createdAt), count(c.id) " +
                            "from Complaint c, Order o join o.store s " +
                            "where c.orderNumber = str(o.orderId) " +
                            "and c.createdAt >= :start and c.createdAt < :end " +
                            "and s.storeId = :sid " +
                            "group by function('date', c.createdAt) " +
                            "order by function('date', c.createdAt)";
        } else {
            jpql =
                    "select function('date', c.createdAt), count(c.id) " +
                            "from Complaint c " +
                            "where c.createdAt >= :start and c.createdAt < :end " +
                            "group by function('date', c.createdAt) " +
                            "order by function('date', c.createdAt)";
        }

        var q = session.createQuery(jpql, Object[].class)
                // Bind Instants so types match Complaint.createdAt
                .setParameter("start", startI)
                .setParameter("end", endI);
        if (branch) {
            q.setParameter("sid", req.getStoreId().intValue()); // Store.storeId is Integer
        }

        java.util.List<Object[]> rows = q.getResultList();

        // Densify range so missing days show as 0
        java.time.LocalDate from = req.getFrom();
        java.time.LocalDate to   = req.getTo();
        java.util.Map<java.time.LocalDate, Long> perDay = new java.util.LinkedHashMap<>();
        for (java.time.LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            perDay.put(d, 0L);
        }
        for (Object[] r : rows) {
            java.sql.Date daySql = (java.sql.Date) r[0];
            java.time.LocalDate day = daySql.toLocalDate();
            long count = ((Number) r[1]).longValue();
            perDay.put(day, count);
        }

        long total = 0L;
        for (var e : perDay.entrySet()) {
            out.getData().add(new ReportDatum(e.getKey().toString(), e.getValue().doubleValue()));
            total += e.getValue();
        }
        out.getMeta().put("totalComplaints", total);
        out.getMeta().put("breakdown", "per_day");
        out.getMeta().put("label", "date");
        out.getMeta().put("from", from.toString());
        out.getMeta().put("to", to.toString());

        return out;
    }




    /** Compare two ranges (same type/scope/store) */
    public ReportCompareResponse compare(ReportCompareRequest req, User currentUser) {
        if (req == null || req.getA() == null || req.getB() == null)
            throw new IllegalArgumentException("Compare request requires both ranges");

        var left = run(req.getA(), currentUser);
        var right = run(req.getB(), currentUser);

        var out = new ReportCompareResponse();
        out.setLeft(left);
        out.setRight(right);

        Map<String, Double> L = new HashMap<>();
        Map<String, Double> R = new HashMap<>();
        for (var d : left.getData())  L.put(d.getLabel(), d.getValue());
        for (var d : right.getData()) R.put(d.getLabel(), d.getValue());

        Set<String> all = new TreeSet<>();
        all.addAll(L.keySet());
        all.addAll(R.keySet());

        for (String label : all) {
            double lv = L.getOrDefault(label, 0.0);
            double rv = R.getOrDefault(label, 0.0);
            out.getDiffs().add(new ReportCompareResponse.CompareDatum(label, lv, rv));
        }
        return out;
    }
}
