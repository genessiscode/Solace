package econest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class ReportManager {

    private static final String REVENUE_FILTER =
            "o.status IN ('CONFIRMED', 'DISPATCHED', 'DELIVERED')";

    public MonthlySalesReport getMonthlySalesReport(int year, int month) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String sql = "SELECT COUNT(*) AS total_orders, "
                + "SUM(CASE WHEN " + REVENUE_FILTER + " THEN 1 ELSE 0 END) "
                + "AS revenue_orders, "
                + "SUM(CASE WHEN o.status = 'PENDING' THEN 1 ELSE 0 END) "
                + "AS pending_orders, "
                + "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) "
                + "AS cancelled_orders, "
                + "COALESCE(SUM(CASE WHEN " + REVENUE_FILTER
                + " THEN o.total_amount ELSE 0 END), 0) AS total_revenue, "
                + "COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' "
                + "THEN o.total_amount ELSE 0 END), 0) AS delivered_revenue "
                + "FROM orders o "
                + "WHERE o.order_date BETWEEN ? AND ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    int revenueOrders = resultSet.getInt("revenue_orders");
                    double revenue = resultSet.getDouble("total_revenue");

                    return new MonthlySalesReport(
                            year,
                            month,
                            resultSet.getInt("total_orders"),
                            revenueOrders,
                            resultSet.getInt("pending_orders"),
                            resultSet.getInt("cancelled_orders"),
                            revenue,
                            resultSet.getDouble("delivered_revenue"),
                            revenueOrders == 0 ? 0.0 : revenue / revenueOrders
                    );
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not build the sales report for " + yearMonth
                            + ": " + e.getMessage(),
                    e
            );
        }

        return new MonthlySalesReport(
                year, month, 0, 0, 0, 0, 0.0, 0.0, 0.0);
    }

    public ArrayList<OrderStatusCount> getOrderStatusBreakdown(
            int year, int month) {

        ArrayList<OrderStatusCount> list = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String sql = "SELECT o.status, COUNT(*) AS order_count, "
                + "COALESCE(SUM(o.total_amount), 0) AS revenue "
                + "FROM orders o "
                + "WHERE o.order_date BETWEEN ? AND ? "
                + "GROUP BY o.status "
                + "ORDER BY CASE o.status "
                + "WHEN 'PENDING' THEN 1 "
                + "WHEN 'CONFIRMED' THEN 2 "
                + "WHEN 'DISPATCHED' THEN 3 "
                + "WHEN 'DELIVERED' THEN 4 "
                + "WHEN 'CANCELLED' THEN 5 "
                + "ELSE 6 END";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    list.add(new OrderStatusCount(
                            resultSet.getString("status"),
                            resultSet.getInt("order_count"),
                            resultSet.getDouble("revenue")
                    ));
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the order status breakdown for "
                            + yearMonth + ": " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public ArrayList<CategorySalesSummary> getSalesByCategory(
            int year, int month) {

        ArrayList<CategorySalesSummary> list = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String sql = "SELECT p.category, "
                + "COALESCE(SUM(oi.quantity), 0) AS units, "
                + "COALESCE(SUM(oi.line_total), 0) AS revenue "
                + "FROM order_items oi "
                + "JOIN orders o ON o.order_id = oi.order_id "
                + "JOIN products p ON p.product_id = oi.product_id "
                + "WHERE o.order_date BETWEEN ? AND ? "
                + "AND " + REVENUE_FILTER + " "
                + "GROUP BY p.category "
                + "ORDER BY revenue DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    list.add(new CategorySalesSummary(
                            resultSet.getString("category"),
                            resultSet.getInt("units"),
                            resultSet.getDouble("revenue")
                    ));
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read sales by category for " + yearMonth
                            + ": " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public ArrayList<TopSellingProduct> getTopSellingProducts(
            int year, int month, int limit) {

        ArrayList<TopSellingProduct> list = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String sql = "SELECT p.product_id, p.name, p.category, "
                + "COALESCE(SUM(oi.quantity), 0) AS units, "
                + "COALESCE(SUM(oi.line_total), 0) AS revenue "
                + "FROM order_items oi "
                + "JOIN orders o ON o.order_id = oi.order_id "
                + "JOIN products p ON p.product_id = oi.product_id "
                + "WHERE o.order_date BETWEEN ? AND ? "
                + "AND " + REVENUE_FILTER + " "
                + "GROUP BY p.product_id, p.name, p.category "
                + "ORDER BY revenue DESC, units DESC, p.name "
                + "LIMIT ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));
            statement.setInt(3, Math.max(limit, 1));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    list.add(new TopSellingProduct(
                            resultSet.getString("product_id"),
                            resultSet.getString("name"),
                            resultSet.getString("category"),
                            resultSet.getInt("units"),
                            resultSet.getDouble("revenue")
                    ));
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read top products for " + yearMonth
                            + ": " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public ArrayList<LowStockItem> getLowStockItems() {

        ArrayList<LowStockItem> list = new ArrayList<>();

        String sql = "SELECT p.product_id, p.name, i.quantity, "
                + "i.reorder_level, "
                + "(i.reorder_level - i.quantity) AS shortfall, "
                + "i.supplier_name "
                + "FROM inventory i "
                + "JOIN products p ON p.product_id = i.product_id "
                + "WHERE i.quantity <= i.reorder_level "
                + "ORDER BY shortfall DESC, p.name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                list.add(new LowStockItem(
                        resultSet.getString("product_id"),
                        resultSet.getString("name"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("reorder_level"),
                        resultSet.getInt("shortfall"),
                        resultSet.getString("supplier_name")
                ));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read low-stock items: " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    public ArrayList<YearMonth> getOrderMonths() {

        ArrayList<YearMonth> months = new ArrayList<>();

        String sql = "SELECT DISTINCT YEAR(o.order_date) AS order_year, "
                + "MONTH(o.order_date) AS order_month "
                + "FROM orders o "
                + "ORDER BY order_year DESC, order_month DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                months.add(YearMonth.of(
                        resultSet.getInt("order_year"),
                        resultSet.getInt("order_month")
                ));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Could not read the months with orders: " + e.getMessage(),
                    e
            );
        }

        return months;
    }

    public static class MonthlySalesReport {

        private final int year;
        private final int month;
        private final int totalOrderCount;
        private final int revenueOrderCount;
        private final int pendingOrderCount;
        private final int cancelledOrderCount;
        private final double totalRevenue;
        private final double deliveredRevenue;
        private final double averageOrderValue;

        public MonthlySalesReport(
                int year,
                int month,
                int totalOrderCount,
                int revenueOrderCount,
                int pendingOrderCount,
                int cancelledOrderCount,
                double totalRevenue,
                double deliveredRevenue,
                double averageOrderValue) {

            this.year = year;
            this.month = month;
            this.totalOrderCount = totalOrderCount;
            this.revenueOrderCount = revenueOrderCount;
            this.pendingOrderCount = pendingOrderCount;
            this.cancelledOrderCount = cancelledOrderCount;
            this.totalRevenue = totalRevenue;
            this.deliveredRevenue = deliveredRevenue;
            this.averageOrderValue = averageOrderValue;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public YearMonth getYearMonth() {
            return YearMonth.of(year, month);
        }

        public int getTotalOrderCount() {
            return totalOrderCount;
        }

        public int getRevenueOrderCount() {
            return revenueOrderCount;
        }

        public int getPendingOrderCount() {
            return pendingOrderCount;
        }

        public int getCancelledOrderCount() {
            return cancelledOrderCount;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public double getDeliveredRevenue() {
            return deliveredRevenue;
        }

        public double getAverageOrderValue() {
            return averageOrderValue;
        }
    }

    public static class OrderStatusCount {

        private final String status;
        private final int orderCount;
        private final double revenue;

        public OrderStatusCount(
                String status, int orderCount, double revenue) {

            this.status = status;
            this.orderCount = orderCount;
            this.revenue = revenue;
        }

        public String getStatus() {
            return status;
        }

        public int getOrderCount() {
            return orderCount;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static class CategorySalesSummary {

        private final String category;
        private final int units;
        private final double revenue;

        public CategorySalesSummary(
                String category, int units, double revenue) {

            this.category = category;
            this.units = units;
            this.revenue = revenue;
        }

        public String getCategory() {
            return category;
        }

        public int getUnits() {
            return units;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static class TopSellingProduct {

        private final String productId;
        private final String name;
        private final String category;
        private final int units;
        private final double revenue;

        public TopSellingProduct(
                String productId,
                String name,
                String category,
                int units,
                double revenue) {

            this.productId = productId;
            this.name = name;
            this.category = category;
            this.units = units;
            this.revenue = revenue;
        }

        public String getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public int getUnits() {
            return units;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static class LowStockItem {

        private final String productId;
        private final String name;
        private final int quantity;
        private final int reorderLevel;
        private final int shortfall;
        private final String supplierName;

        public LowStockItem(
                String productId,
                String name,
                int quantity,
                int reorderLevel,
                int shortfall,
                String supplierName) {

            this.productId = productId;
            this.name = name;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
            this.shortfall = shortfall;
            this.supplierName = supplierName;
        }

        public String getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getReorderLevel() {
            return reorderLevel;
        }

        public int getShortfall() {
            return shortfall;
        }

        public String getSupplierName() {
            return supplierName;
        }
    }
}
