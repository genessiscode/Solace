package econest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ReportFrame extends JFrame {

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final ReportManager reportManager = new ReportManager();

    private final JComboBox<MonthOption> monthCombo = new JComboBox<>();
    private JButton refreshButton;

    private final StatCard revenueCard =
            new StatCard("Revenue", UITheme.LEAF);
    private final StatCard ordersCard =
            new StatCard("Orders", UITheme.MOSS);
    private final StatCard averageCard =
            new StatCard("Avg order value", UITheme.FOREST);
    private final StatCard lowStockCard =
            new StatCard("Low-stock alerts", UITheme.WARNING);

    private final DefaultTableModel statusModel = readOnlyModel(
            "Status", "Orders", "Revenue (Rs.)");
    private final DefaultTableModel categoryModel = readOnlyModel(
            "Category", "Units", "Revenue (Rs.)", "Share");
    private final DefaultTableModel topModel = readOnlyModel(
            "Rank", "Product", "Category", "Units", "Revenue (Rs.)");
    private final DefaultTableModel lowStockModel = readOnlyModel(
            "Product ID", "Product", "Stock", "Reorder",
            "Gap", "Supplier");

    private final JTable statusTable = new JTable(statusModel);
    private final JTable categoryTable = new JTable(categoryModel);
    private final JTable topTable = new JTable(topModel);
    private final JTable lowStockTable = new JTable(lowStockModel);

    private final JLabel lowStockHint =
            UITheme.hint("At or below reorder level");
    private final JLabel statusLabel = UITheme.hint("Loading reports...");

    private boolean loading;

    public ReportFrame() {

        setTitle("EcoNest - Monthly Reports");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1360, 900);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);

        configureTables();
        setContentPane(createContent());

        populateMonths();
        setVisible(true);
        loadReport();
    }

    private void configureTables() {

        styleTable(statusTable);
        styleTable(categoryTable);
        styleTable(topTable);
        styleTable(lowStockTable);

        setColumnWidths(statusTable, 120, 90, 180);
        setColumnWidths(categoryTable, 120, 80, 160, 90);
        setColumnWidths(topTable, 60, 260, 100, 80, 150);
        setColumnWidths(lowStockTable, 90, 170, 65, 65, 55, 145);

        statusTable.getColumnModel().getColumn(0)
                .setCellRenderer(UITheme.statusPillRenderer());
    }

    private static void setColumnWidths(JTable table, int... widths) {

        for (int i = 0; i < widths.length
                && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private static void styleTable(JTable table) {

        UITheme.styleTable(table);
        table.setFillsViewportHeight(true);
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createReportGrid(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeaderPanel() {

        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);

        header.add(new GreenHeader(
                "Monthly Reports",
                "Revenue, sales mix and low-stock alerts for the selected month."
        ), BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0, 14));
        middle.setOpaque(false);
        middle.add(createFilterCard(), BorderLayout.NORTH);
        middle.add(createStatsPanel(), BorderLayout.CENTER);

        header.add(middle, BorderLayout.CENTER);

        return header;
    }

    private RoundedPanel createFilterCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(12, 0));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(UITheme.fieldLabel("REPORTING MONTH"));

        UITheme.styleCombo(monthCombo);
        monthCombo.setPreferredSize(new Dimension(190, 38));
        monthCombo.addActionListener(e -> {
            if (!loading) {
                loadReport();
            }
        });
        controls.add(monthCombo);

        refreshButton = UITheme.secondaryButton("Refresh");
        refreshButton.addActionListener(e -> {
            populateMonths();
            loadReport();
        });
        controls.add(refreshButton);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        status.setOpaque(false);
        status.add(statusLabel);

        card.add(controls, BorderLayout.WEST);
        card.add(status, BorderLayout.EAST);

        return card;
    }

    private JPanel createStatsPanel() {

        revenueCard.setValue("Rs. 0.00");
        revenueCard.setHint("No revenue recorded");
        ordersCard.setValue(0);
        ordersCard.setHint("No orders recorded");
        averageCard.setValue("Rs. 0.00");
        averageCard.setHint("Per revenue order");
        lowStockCard.setValue(0);
        lowStockCard.setHint("At or below reorder level");

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(revenueCard);
        stats.add(ordersCard);
        stats.add(averageCard);
        stats.add(lowStockCard);

        return stats;
    }

    private JPanel createReportGrid() {

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        grid.add(tableCard(
                "Revenue by status",
                UITheme.hint("Order value grouped by current status"),
                statusTable));
        grid.add(tableCard(
                "Sales by category",
                UITheme.hint("Order-line revenue for the selected month"),
                categoryTable));
        grid.add(tableCard(
                "Top products",
                UITheme.hint("Best sellers by revenue this month"),
                topTable));
        grid.add(tableCard(
                "Low-stock alerts",
                lowStockHint,
                lowStockTable));

        return grid;
    }

    private RoundedPanel tableCard(
            String title, JLabel hint, JTable table) {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        heading.add(UITheme.sectionTitle(title));
        heading.add(hint);

        card.add(heading, BorderLayout.NORTH);
        card.add(UITheme.scroll(table), BorderLayout.CENTER);

        return card;
    }

    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(UITheme.hint(
                "Revenue excludes pending and cancelled orders."),
                BorderLayout.WEST);
        footer.add(UITheme.hint("EcoNest - Monthly Reports"),
                BorderLayout.EAST);

        return footer;
    }

    private void populateMonths() {

        YearMonth current = selectedMonth();
        YearMonth now = YearMonth.now();
        ArrayList<YearMonth> dataMonths = new ArrayList<>();

        try {
            dataMonths = reportManager.getOrderMonths();
        } catch (IllegalStateException ignored) {

        }

        LinkedHashSet<YearMonth> months = new LinkedHashSet<>();

        for (int i = 0; i < 12; i++) {
            months.add(now.minusMonths(i));
        }

        months.addAll(dataMonths);

        ArrayList<YearMonth> sorted = new ArrayList<>(months);
        sorted.sort(Comparator.reverseOrder());

        YearMonth preferred = current;

        if (preferred == null) {
            preferred = dataMonths.isEmpty() ? now : dataMonths.get(0);
        }

        loading = true;
        monthCombo.removeAllItems();

        for (YearMonth month : sorted) {
            monthCombo.addItem(new MonthOption(month));
        }

        selectMonth(preferred);
        loading = false;
    }

    private void loadReport() {

        MonthOption option = (MonthOption) monthCombo.getSelectedItem();

        if (option == null || loading) {
            return;
        }

        YearMonth month = option.month;
        YearMonth previous = month.minusMonths(1);

        setLoading(true, "Loading " + format(month) + "...");

        SwingWorker<ReportData, Void> worker =
                new SwingWorker<ReportData, Void>() {

            @Override
            protected ReportData doInBackground() {

                return new ReportData(
                        reportManager.getMonthlySalesReport(
                                month.getYear(), month.getMonthValue()),
                        reportManager.getMonthlySalesReport(
                                previous.getYear(), previous.getMonthValue()),
                        reportManager.getOrderStatusBreakdown(
                                month.getYear(), month.getMonthValue()),
                        reportManager.getSalesByCategory(
                                month.getYear(), month.getMonthValue()),
                        reportManager.getTopSellingProducts(
                                month.getYear(), month.getMonthValue(), 8),
                        reportManager.getLowStockItems()
                );
            }

            @Override
            protected void done() {

                try {
                    render(get(), month, previous);
                    setLoading(false, doneMessage(month, statusModel));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setLoading(false, "Report interrupted");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    resetReport();
                    setLoading(false, "Report unavailable");
                    showError(cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void render(
            ReportData data, YearMonth month, YearMonth previous) {

        ReportManager.MonthlySalesReport current = data.current();

        revenueCard.setValue("Rs. " + money(current.getTotalRevenue()));
        revenueCard.setHint(compareHint(
                current.getTotalRevenue(),
                data.previous().getTotalRevenue(),
                previous));

        ordersCard.setValue(current.getRevenueOrderCount());
        ordersCard.setHint(orderHint(current));

        averageCard.setValue("Rs. " + money(current.getAverageOrderValue()));
        averageCard.setHint("Across "
                + current.getRevenueOrderCount() + " revenue orders");

        lowStockCard.setValue(data.lowStock().size());
        lowStockCard.setHint(data.lowStock().isEmpty()
                ? "All products above reorder level"
                : "At or below reorder level");

        renderStatus(data.statuses());
        renderCategories(data.categories());
        renderTopProducts(data.topProducts());
        renderLowStock(data.lowStock());
    }

    private void renderStatus(
            ArrayList<ReportManager.OrderStatusCount> statuses) {

        statusModel.setRowCount(0);

        for (ReportManager.OrderStatusCount status : statuses) {
            statusModel.addRow(new Object[]{
                    status.getStatus(),
                    status.getOrderCount(),
                    money(status.getRevenue())
            });
        }
    }

    private void renderCategories(
            ArrayList<ReportManager.CategorySalesSummary> categories) {

        categoryModel.setRowCount(0);

        double total = 0;

        for (ReportManager.CategorySalesSummary category : categories) {
            total += category.getRevenue();
        }

        for (ReportManager.CategorySalesSummary category : categories) {

            double share = total == 0 ? 0 : category.getRevenue() / total * 100;

            categoryModel.addRow(new Object[]{
                    category.getCategory(),
                    category.getUnits(),
                    money(category.getRevenue()),
                    String.format(Locale.ROOT, "%.1f%%", share)
            });
        }
    }

    private void renderTopProducts(
            ArrayList<ReportManager.TopSellingProduct> products) {

        topModel.setRowCount(0);
        int rank = 1;

        for (ReportManager.TopSellingProduct product : products) {
            topModel.addRow(new Object[]{
                    rank++,
                    product.getProductId() + " - " + product.getName(),
                    product.getCategory(),
                    product.getUnits(),
                    money(product.getRevenue())
            });
        }
    }

    private void renderLowStock(
            ArrayList<ReportManager.LowStockItem> items) {

        lowStockModel.setRowCount(0);

        for (ReportManager.LowStockItem item : items) {
            lowStockModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getReorderLevel(),
                    item.getShortfall(),
                    item.getSupplierName() == null
                            ? "-" : item.getSupplierName()
            });
        }

        if (items.isEmpty()) {
            lowStockHint.setText(
                    "All products are above their reorder level");
        } else {
            lowStockHint.setText(
                    items.size() + " product(s) need reordering");
        }
    }

    private void resetReport() {

        revenueCard.setValue("Rs. 0.00");
        revenueCard.setHint("No revenue recorded");
        ordersCard.setValue(0);
        ordersCard.setHint("No orders recorded");
        averageCard.setValue("Rs. 0.00");
        averageCard.setHint("Per revenue order");
        lowStockCard.setValue(0);
        lowStockCard.setHint("At or below reorder level");

        statusModel.setRowCount(0);
        categoryModel.setRowCount(0);
        topModel.setRowCount(0);
        lowStockModel.setRowCount(0);
        lowStockHint.setText("At or below reorder level");
    }

    private void setLoading(boolean busy, String message) {

        loading = busy;
        monthCombo.setEnabled(!busy);

        if (refreshButton != null) {
            refreshButton.setEnabled(!busy);
        }

        if (message != null) {
            statusLabel.setText(message);
        }
    }

    private YearMonth selectedMonth() {

        Object selected = monthCombo.getSelectedItem();

        return selected instanceof MonthOption
                ? ((MonthOption) selected).month : null;
    }

    private void selectMonth(YearMonth month) {

        for (int i = 0; i < monthCombo.getItemCount(); i++) {
            MonthOption option = monthCombo.getItemAt(i);

            if (option.month.equals(month)) {
                monthCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static String doneMessage(
            YearMonth month, DefaultTableModel statusModel) {

        if (statusModel.getRowCount() == 0) {
            return "No orders recorded in " + format(month);
        }

        return "Showing " + format(month);
    }

    private static String compareHint(
            double current, double previous, YearMonth previousMonth) {

        if (previous <= 0) {
            return "No revenue in " + format(previousMonth);
        }

        double change = (current - previous) / previous * 100;
        String arrow = change >= 0 ? "+" : "";

        return String.format(
                Locale.ROOT,
                "%s%.1f%% vs %s",
                arrow,
                change,
                format(previousMonth));
    }

    private static String orderHint(
            ReportManager.MonthlySalesReport report) {

        int pending = report.getPendingOrderCount();
        int cancelled = report.getCancelledOrderCount();

        if (pending == 0 && cancelled == 0) {
            return "No pending or cancelled orders";
        }

        return pending + " pending, " + cancelled + " cancelled";
    }

    private static String money(double value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }

    private static String format(YearMonth month) {
        return month.format(MONTH_FORMAT);
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message == null
                        ? "An unexpected error occurred while building the report."
                        : message,
                "Monthly Reports",
                JOptionPane.ERROR_MESSAGE);
    }

    private static DefaultTableModel readOnlyModel(String... columns) {

        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private record ReportData(
            ReportManager.MonthlySalesReport current,
            ReportManager.MonthlySalesReport previous,
            ArrayList<ReportManager.OrderStatusCount> statuses,
            ArrayList<ReportManager.CategorySalesSummary> categories,
            ArrayList<ReportManager.TopSellingProduct> topProducts,
            ArrayList<ReportManager.LowStockItem> lowStock) {
    }

    private static final class MonthOption {

        private final YearMonth month;

        private MonthOption(YearMonth month) {
            this.month = month;
        }

        @Override
        public String toString() {
            return month.format(MONTH_FORMAT);
        }
    }
}
