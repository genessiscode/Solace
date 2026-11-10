package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportManagerTest {

    private ReportManager manager;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping report integration test");
        manager = new ReportManager();
    }

    @Test
    void septemberSalesTotalsMatchSeedOrders() {
        ReportManager.MonthlySalesReport report =
                manager.getMonthlySalesReport(2026, 9);

        assertEquals(2, report.getTotalOrderCount());
        assertEquals(2, report.getRevenueOrderCount());
        assertEquals(0, report.getPendingOrderCount());
        assertEquals(0, report.getCancelledOrderCount());
        assertEquals(21100.00, report.getTotalRevenue(), 0.01);
        assertEquals(6700.00, report.getDeliveredRevenue(), 0.01);
        assertEquals(10550.00, report.getAverageOrderValue(), 0.01);
    }

    @Test
    void statusBreakdownSplitsOrdersAndRevenue() {
        ArrayList<ReportManager.OrderStatusCount> statuses =
                manager.getOrderStatusBreakdown(2026, 9);

        assertEquals(2, statuses.size());
        assertEquals(1, findStatus(statuses, "DELIVERED").getOrderCount());
        assertEquals(6700.00,
                findStatus(statuses, "DELIVERED").getRevenue(), 0.01);
        assertEquals(1, findStatus(statuses, "DISPATCHED").getOrderCount());
        assertEquals(14400.00,
                findStatus(statuses, "DISPATCHED").getRevenue(), 0.01);
    }

    @Test
    void topProductsRankSeededSeptemberByRevenue() {
        ArrayList<ReportManager.TopSellingProduct> products =
                manager.getTopSellingProducts(2026, 9, 5);

        assertFalse(products.isEmpty());
        assertEquals("P008", products.get(0).getProductId());
        assertEquals(9600.00, products.get(0).getRevenue(), 0.01);
        assertEquals("P001", products.get(1).getProductId());
        assertEquals(7500.00, products.get(1).getRevenue(), 0.01);
    }

    @Test
    void categorySalesCoverAllSeededCategories() {
        ArrayList<ReportManager.CategorySalesSummary> categories =
                manager.getSalesByCategory(2026, 9);

        assertEquals(3, categories.size());
        assertEquals(14800.00,
                findCategory(categories, "KIT").getRevenue(), 0.01);
        assertEquals(8700.00,
                findCategory(categories, "PLANT").getRevenue(), 0.01);
        assertEquals(2100.00,
                findCategory(categories, "PLANTER").getRevenue(), 0.01);
    }

    @Test
    void lowStockAlertsMatchSeedInventory() {
        ArrayList<ReportManager.LowStockItem> items =
                manager.getLowStockItems();

        assertEquals(3, items.size());
        assertEquals(2, findLowStock(items, "P003").getShortfall());
        assertEquals(2, findLowStock(items, "P009").getShortfall());
        assertEquals(1, findLowStock(items, "P010").getShortfall());
        assertTrue(items.stream().noneMatch(
                item -> "P008".equals(item.getProductId())),
                "P008 has 6 units against a reorder level of 5");
    }

    @Test
    void monthsWithOrdersIncludeSeededMonthsNewestFirst() {
        ArrayList<YearMonth> months = manager.getOrderMonths();

        assertTrue(months.contains(YearMonth.of(2026, 9)));
        assertTrue(months.contains(YearMonth.of(2026, 10)));
        assertEquals(YearMonth.of(2026, 10), months.get(0));
    }

    private static ReportManager.OrderStatusCount findStatus(
            ArrayList<ReportManager.OrderStatusCount> statuses,
            String status) {

        return statuses.stream()
                .filter(item -> status.equals(item.getStatus()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Missing status " + status));
    }

    private static ReportManager.CategorySalesSummary findCategory(
            ArrayList<ReportManager.CategorySalesSummary> categories,
            String category) {

        return categories.stream()
                .filter(item -> category.equals(item.getCategory()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Missing category " + category));
    }

    private static ReportManager.LowStockItem findLowStock(
            ArrayList<ReportManager.LowStockItem> items,
            String productId) {

        return items.stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Missing low-stock product " + productId));
    }
}
