package econest;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class MainFrame extends JFrame {

    private static final String VIEW_OVERVIEW = "overview";
    private static final String VIEW_MODULE = "module";
    private static final int SIDEBAR_WIDTH = 244;

    private final CardLayout viewLayout = new CardLayout();
    private final JPanel viewHost = new JPanel(viewLayout);
    private final JPanel moduleHost = new JPanel(new BorderLayout());
    private final List<NavEntry> navEntries = new ArrayList<>();

    private final JLabel productsValue = metricValue();
    private final JLabel clientsValue = metricValue();
    private final JLabel openOrdersValue = metricValue();
    private final JLabel lowStockValue = metricValue();
    private final JLabel deliveryLine = UITheme.hint("");
    private final JLabel revenueLine = UITheme.hint("");
    private final List<RowEntry> rowEntries = new ArrayList<>();
    private final DefaultTableModel recentModel = new DefaultTableModel(
            new Object[]{"Order", "Client", "Date", "Status", "Total (Rs.)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable recentTable = new JTable(recentModel);
    private final JPanel lowStockHost = new JPanel();
    private final JLabel lowStockEmpty = UITheme.hint("Everything is above the reorder level.");

    private JLabel workspaceTitle;
    private JLabel workspaceSubtitle;
    private JButton openButton;
    private JPanel errorStrip;
    private Module currentModule;
    private NavEntry overviewEntry;

    private record Module(
            String number,
            String title,
            String description,
            String tables,
            boolean built,
            Supplier<JFrame> frameFactory) {
    }

    private record NavEntry(JPanel panel, Module module, boolean overview) {
    }

    private record RowEntry(Module module, JLabel detail) {
    }

    private record Snapshot(int products, int clients, int openOrders, int lowStock,
                            int agents, int deliveries, String revenueLabel,
                            List<Object[]> recentOrders, List<String> lowStockItems) {
    }

    private static final List<Module> MODULES = List.of(
            new Module("01", "Product Catalogue",
                    "Plants, planters and garden kits.",
                    "products",
                    true, ProductCatalogueFrame::new),
            new Module("02", "Client Management",
                    "Household and office clients.",
                    "clients",
                    true, CustomerManagementFrame::new),
            new Module("03", "Stock / Inventory",
                    "Quantities, reorder levels and suppliers.",
                    "inventory, products",
                    true, InventoryFrame::new),
            new Module("04", "Delivery Agents",
                    "Agents, availability and vehicles.",
                    "agents, vehicles",
                    true, AgentFrame::new),
            new Module("05", "Client Orders",
                    "Orders, line items and totals.",
                    "orders, order_items, clients, products",
                    true, OrderFrame::new),
            new Module("06", "Delivery Assignment",
                    "Assign orders and track each delivery.",
                    "deliveries, orders, agents",
                    true, DeliveryFrame::new),
            new Module("07", "Monthly Reports",
                    "Sales and inventory summaries.",
                    "orders, order_items, products, inventory",
                    true, ReportFrame::new),
            new Module("08", "Client Notifications",
                    "Email clients when an order is dispatched.",
                    "notifications",
                    false, null),
            new Module("09", "Agent Notifications",
                    "Email agents when a delivery is assigned.",
                    "notifications",
                    false, null)
    );

    public MainFrame() {

        setTitle("EcoNest");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 860);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setContentPane(createContent());
        showOverview();
        setVisible(true);

        loadSnapshot();
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createWorkspace(), BorderLayout.CENTER);
        return root;
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.DEEP);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 18, 18, 18));

        JLabel brand = new JLabel("EcoNest");
        brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Home & Garden Management");
        tagline.setFont(UITheme.SMALL);
        tagline.setForeground(UITheme.SAGE);
        tagline.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(24));

        overviewEntry = navEntry("Overview", null, true);
        sidebar.add(overviewEntry.panel());

        sidebar.add(sectionLabel("Operations"));
        for (Module module : MODULES) {
            if (module.number().compareTo("05") <= 0) {
                sidebar.add(navEntry(module.title(), module, false).panel());
            }
        }

        sidebar.add(sectionLabel("Logistics"));
        sidebar.add(navEntry("Delivery Assignment",
                find("06"), false).panel());
        sidebar.add(navEntry("Monthly Reports",
                find("07"), false).panel());

        sidebar.add(sectionLabel("In development"));
        sidebar.add(navEntry("Client Notifications", find("08"), false).panel());
        sidebar.add(navEntry("Agent Notifications", find("09"), false).panel());

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private Module find(String number) {
        for (Module module : MODULES) {
            if (module.number().equals(number)) return module;
        }
        return null;
    }

    private JLabel sectionLabel(String text) {

        JLabel label = new JLabel(text.toUpperCase(Locale.ROOT));
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(UITheme.MOSS);
        label.setBorder(BorderFactory.createEmptyBorder(20, 4, 6, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private NavEntry navEntry(String title, Module module, boolean overview) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.DEEP);
        panel.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        boolean enabled = module == null || module.built();

        JLabel label = new JLabel((module == null ? "" : module.number() + "   ") + title);
        label.setFont(UITheme.BODY_BOLD);
        label.setForeground(enabled ? Color.WHITE : UITheme.SAGE);
        panel.add(label, BorderLayout.CENTER);

        NavEntry entry = new NavEntry(panel, module, overview);
        panel.setCursor(enabled
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());

        if (enabled) {
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (panel.getBackground() == UITheme.DEEP) {
                        panel.setBackground(UITheme.FOREST);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (panel.getClientProperty("selected") != Boolean.TRUE) {
                        panel.setBackground(UITheme.DEEP);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (overview) {
                        showOverview();
                    } else {
                        showModule(module, entry);
                    }
                }
            });
        }

        navEntries.add(entry);
        return entry;
    }

    // ================= WORKSPACE =================

    private JPanel createWorkspace() {

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UITheme.BG);
        workspace.add(createTopBar(), BorderLayout.NORTH);

        viewHost.setBackground(UITheme.BG);
        viewHost.setBorder(BorderFactory.createEmptyBorder(0, 26, 8, 26));
        viewHost.add(createOverview(), VIEW_OVERVIEW);
        viewHost.add(moduleHost, VIEW_MODULE);

        workspace.add(viewHost, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createTopBar() {

        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(UITheme.CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 26, 16, 26)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        workspaceTitle = new JLabel("Overview");
        workspaceTitle.setFont(UITheme.H2);
        workspaceTitle.setForeground(UITheme.DEEP);

        workspaceSubtitle = UITheme.hint("Product catalogue, clients, stock, delivery and reports.");
        text.add(workspaceTitle);
        text.add(Box.createVerticalStrut(2));
        text.add(workspaceSubtitle);

        openButton = UITheme.primaryButton("Open module");
        openButton.setPreferredSize(new Dimension(170, 40));
        openButton.setVisible(false);
        openButton.addActionListener(e -> openCurrentModule());

        bar.add(text, BorderLayout.WEST);
        bar.add(openButton, BorderLayout.EAST);
        return bar;
    }

    private JPanel createOverview() {

        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setBackground(UITheme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(20, 0, 16, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createErrorStrip());
        top.add(createMetricsBar());
        top.add(Box.createVerticalStrut(12));
        top.add(createMainArea());

        content.add(top, BorderLayout.NORTH);
        content.add(createRecentOrders(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createErrorStrip() {

        errorStrip = new JPanel(new BorderLayout(12, 0));
        errorStrip.setBackground(UITheme.DANGER_BG);
        errorStrip.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        errorStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        errorStrip.setAlignmentX(LEFT_ALIGNMENT);
        errorStrip.setVisible(false);

        JLabel message = new JLabel("Database unreachable - check config.properties");
        message.setFont(UITheme.BODY_BOLD);
        message.setForeground(UITheme.DANGER);

        JButton retry = UITheme.dangerButton("Retry");
        retry.addActionListener(e -> loadSnapshot());

        errorStrip.add(message, BorderLayout.CENTER);
        errorStrip.add(retry, BorderLayout.EAST);
        return errorStrip;
    }

    // ================= REAL DATA =================

    private static JLabel metricValue() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("SansSerif", Font.BOLD, 30));
        label.setForeground(UITheme.DEEP);
        return label;
    }

    private JPanel createMetricsBar() {

        RoundedPanel bar = new RoundedPanel(UITheme.CARD, 16);
        bar.setLayout(new GridLayout(1, 4, 0, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        bar.setBorderColor(UITheme.BORDER);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        bar.setAlignmentX(LEFT_ALIGNMENT);

        bar.add(metric("Products", productsValue, "in the catalogue"));
        bar.add(metric("Clients", clientsValue, "household and office"));
        bar.add(metric("Open orders", openOrdersValue, "pending or confirmed"));
        bar.add(metric("Low stock", lowStockValue, "at or below reorder level"));
        return bar;
    }

    private JPanel metric(String caption, JLabel value, String hint) {

        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(UITheme.SMALL_BOLD);
        captionLabel.setForeground(UITheme.MUTED);
        captionLabel.setAlignmentX(LEFT_ALIGNMENT);

        value.setAlignmentX(LEFT_ALIGNMENT);
        JLabel hintLabel = UITheme.hint(hint);
        hintLabel.setAlignmentX(LEFT_ALIGNMENT);

        cell.add(captionLabel);
        cell.add(Box.createVerticalStrut(4));
        cell.add(value);
        cell.add(Box.createVerticalStrut(2));
        cell.add(hintLabel);
        return cell;
    }

    private JPanel createMainArea() {

        JPanel area = new JPanel(new BorderLayout(22, 0));
        area.setOpaque(false);
        area.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel heading = new JLabel("Operations");
        heading.setFont(UITheme.H3);
        heading.setForeground(UITheme.DEEP);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        left.add(heading);
        left.add(Box.createVerticalStrut(10));

        for (Module module : MODULES) {
            if (module.number().compareTo("05") <= 0) {
                left.add(createModuleRow(module));
                left.add(Box.createVerticalStrut(5));
            }
        }

        area.add(left, BorderLayout.CENTER);
        area.add(createSideColumn(), BorderLayout.EAST);
        return area;
    }

    private JPanel createModuleRow(Module module) {

        RoundedPanel row = new RoundedPanel(UITheme.CARD, 12);
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        row.setBorderColor(UITheme.BORDER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel number = new JLabel(module.number());
        number.setFont(new Font("Monospaced", Font.BOLD, 13));
        number.setForeground(UITheme.LEAF);
        number.setPreferredSize(new Dimension(26, 0));

        JLabel title = new JLabel(module.title());
        title.setFont(UITheme.BODY_BOLD);
        title.setForeground(UITheme.DEEP);

        JLabel description = UITheme.hint(module.description());
        JLabel detail = UITheme.hint("");
        detail.setForeground(UITheme.FOREST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(description);
        text.add(detail);

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        left.add(number, BorderLayout.WEST);
        left.add(text, BorderLayout.CENTER);

        JLabel arrow = new JLabel("\u2192");
        arrow.setFont(new Font("SansSerif", Font.BOLD, 17));
        arrow.setForeground(UITheme.LEAF);

        row.add(left, BorderLayout.CENTER);
        row.add(arrow, BorderLayout.EAST);
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBorderColor(UITheme.LEAF);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBorderColor(UITheme.BORDER);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                openModule(module);
            }
        });

        rowEntries.add(new RowEntry(module, detail));
        return row;
    }

    private JPanel createSideColumn() {

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(330, 0));

        column.add(tile("Logistics", "Delivery Assignment", find("06"), deliveryLine));
        column.add(Box.createVerticalStrut(12));
        column.add(tile("Insight", "Monthly Reports", find("07"), revenueLine));
        column.add(Box.createVerticalStrut(18));
        column.add(createLowStockPanel());

        JLabel heading = new JLabel("In development");
        heading.setFont(UITheme.H3);
        heading.setForeground(UITheme.DEEP);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 2, 6, 0));
        column.add(heading);

        JLabel pending = UITheme.hint("Client notifications  \u00B7  Agent notifications");
        pending.setAlignmentX(LEFT_ALIGNMENT);
        pending.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        column.add(pending);

        return column;
    }

    private JPanel createLowStockPanel() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Low stock");
        heading.setFont(UITheme.H3);
        heading.setForeground(UITheme.DEEP);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel caption = UITheme.hint("quantity / reorder level");
        caption.setAlignmentX(LEFT_ALIGNMENT);

        lowStockHost.setOpaque(false);
        lowStockHost.setLayout(new BoxLayout(lowStockHost, BoxLayout.Y_AXIS));
        lowStockHost.setAlignmentX(LEFT_ALIGNMENT);
        lowStockEmpty.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(caption);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lowStockHost);
        return panel;
    }

    private RoundedPanel tile(String label, String title, Module module, JLabel detail) {

        RoundedPanel tile = new RoundedPanel(UITheme.DEEP, 16);
        tile.setLayout(new BorderLayout(0, 10));
        tile.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        tile.setAlignmentX(LEFT_ALIGNMENT);
        tile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 148));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel kicker = new JLabel(label.toUpperCase(Locale.ROOT));
        kicker.setFont(new Font("SansSerif", Font.BOLD, 10));
        kicker.setForeground(UITheme.MOSS);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        detail.setForeground(UITheme.SAGE);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(kicker);
        text.add(Box.createVerticalStrut(4));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(detail);

        tile.add(text, BorderLayout.CENTER);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openModule(module);
            }
        });
        return tile;
    }

    private JPanel createRecentOrders() {

        RoundedPanel card = new RoundedPanel(UITheme.CARD, 16);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        card.setBorderColor(UITheme.BORDER);
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Recent orders");
        heading.setFont(UITheme.H3);
        heading.setForeground(UITheme.DEEP);

        UITheme.styleTable(recentTable);
        recentTable.setRowHeight(26);
        recentTable.getTableHeader().setPreferredSize(new Dimension(0, 28));

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD);

        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void loadSnapshot() {

        new SwingWorker<Snapshot, Void>() {
            @Override
            protected Snapshot doInBackground() throws Exception {
                String countsSql = "SELECT "
                        + "(SELECT COUNT(*) FROM products) AS products, "
                        + "(SELECT COUNT(*) FROM clients) AS clients, "
                        + "(SELECT COUNT(*) FROM orders WHERE status IN ('PENDING','CONFIRMED')) AS open_orders, "
                        + "(SELECT COUNT(*) FROM inventory WHERE quantity <= reorder_level) AS low_stock, "
                        + "(SELECT COUNT(*) FROM agents) AS agents, "
                        + "(SELECT COUNT(*) FROM deliveries) AS deliveries";

                List<Object[]> recent = new ArrayList<>();
                List<String> lowStockItems = new ArrayList<>();
                String revenueLabel = "No sales yet";

                try (Connection connection = DBConnection.getConnection()) {
                    int products;
                    int clients;
                    int openOrders;
                    int lowStock;
                    int agents;
                    int deliveries;

                    try (PreparedStatement statement = connection.prepareStatement(countsSql);
                         ResultSet result = statement.executeQuery()) {
                        result.next();
                        products = result.getInt("products");
                        clients = result.getInt("clients");
                        openOrders = result.getInt("open_orders");
                        lowStock = result.getInt("low_stock");
                        agents = result.getInt("agents");
                        deliveries = result.getInt("deliveries");
                    }

                    String revenueSql = "SELECT YEAR(order_date) AS y, MONTH(order_date) AS m, "
                            + "SUM(total_amount) AS total FROM orders "
                            + "WHERE status IN ('CONFIRMED','DISPATCHED','DELIVERED') "
                            + "GROUP BY y, m ORDER BY y DESC, m DESC LIMIT 1";
                    try (PreparedStatement statement = connection.prepareStatement(revenueSql);
                         ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            revenueLabel = String.format(Locale.ROOT, "Rs. %,.0f in %s",
                                    result.getDouble("total"),
                                    Month.of(result.getInt("m"))
                                            .getDisplayName(TextStyle.FULL, Locale.UK));
                        }
                    }

                    String recentSql = "SELECT o.order_id, c.name, o.order_date, o.status, "
                            + "o.total_amount FROM orders o "
                            + "JOIN clients c ON c.client_id = o.client_id "
                            + "ORDER BY o.order_date DESC, o.order_id DESC LIMIT 8";
                    try (PreparedStatement statement = connection.prepareStatement(recentSql);
                         ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            recent.add(new Object[]{
                                    result.getString("order_id"),
                                    result.getString("name"),
                                    result.getDate("order_date").toString(),
                                    result.getString("status"),
                                    String.format(Locale.ROOT, "%,.2f", result.getDouble("total_amount"))});
                        }
                    }

                    String lowSql = "SELECT p.name, i.quantity, i.reorder_level "
                            + "FROM inventory i JOIN products p ON p.product_id = i.product_id "
                            + "WHERE i.quantity <= i.reorder_level "
                            + "ORDER BY (i.quantity - i.reorder_level), p.name LIMIT 4";
                    try (PreparedStatement statement = connection.prepareStatement(lowSql);
                         ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            lowStockItems.add(result.getString("name") + "   "
                                    + result.getInt("quantity") + " / "
                                    + result.getInt("reorder_level"));
                        }
                    }

                    return new Snapshot(products, clients, openOrders, lowStock,
                            agents, deliveries, revenueLabel, recent, lowStockItems);
                }
            }

            @Override
            protected void done() {
                try {
                    render(get());
                } catch (Exception ex) {
                    errorStrip.setVisible(true);
                }
            }
        }.execute();
    }

    private void render(Snapshot snapshot) {

        errorStrip.setVisible(false);

        productsValue.setText(String.valueOf(snapshot.products()));
        clientsValue.setText(String.valueOf(snapshot.clients()));
        openOrdersValue.setText(String.valueOf(snapshot.openOrders()));
        lowStockValue.setText(String.valueOf(snapshot.lowStock()));

        deliveryLine.setText(snapshot.deliveries() + " deliveries on record");
        revenueLine.setText(snapshot.revenueLabel());

        recentModel.setRowCount(0);
        for (Object[] row : snapshot.recentOrders()) {
            recentModel.addRow(row);
        }

        lowStockHost.removeAll();
        if (snapshot.lowStockItems().isEmpty()) {
            lowStockHost.add(lowStockEmpty);
        } else {
            for (String item : snapshot.lowStockItems()) {
                JLabel line = new JLabel(item);
                line.setFont(UITheme.MONO);
                line.setForeground(UITheme.TEXT);
                lowStockHost.add(line);
            }
        }
        lowStockHost.revalidate();
        lowStockHost.repaint();

        for (RowEntry entry : rowEntries) {
            String detail = switch (entry.module().number()) {
                case "01" -> snapshot.products() + " products";
                case "02" -> snapshot.clients() + " clients";
                case "03" -> snapshot.lowStock() + " low stock";
                case "04" -> snapshot.agents() + " agents";
                case "05" -> snapshot.openOrders() + " open orders";
                default -> "";
            };
            entry.detail().setText(detail);
        }
    }

    // ================= MODULE DETAIL =================

    private void showOverview() {

        currentModule = null;
        openButton.setVisible(false);
        workspaceTitle.setText("Overview");
        workspaceSubtitle.setText("Product catalogue, clients, stock, delivery and reports.");
        selectNav(null);
        viewLayout.show(viewHost, VIEW_OVERVIEW);
    }

    private void showModule(Module module, NavEntry entry) {

        if (module == null || !module.built()) return;

        currentModule = module;
        openButton.setVisible(true);
        workspaceTitle.setText(module.title());
        workspaceSubtitle.setText(module.description());
        selectNav(entry);

        moduleHost.removeAll();
        moduleHost.setBackground(UITheme.BG);
        moduleHost.setBorder(BorderFactory.createEmptyBorder(22, 0, 16, 0));
        moduleHost.add(createModuleDetail(module), BorderLayout.NORTH);
        moduleHost.revalidate();
        moduleHost.repaint();
        viewLayout.show(viewHost, VIEW_MODULE);
    }

    private JPanel createModuleDetail(Module module) {

        RoundedPanel panel = new RoundedPanel(UITheme.CARD, 16);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        panel.setBorderColor(UITheme.BORDER);

        JLabel title = new JLabel(module.title());
        title.setFont(UITheme.H1);
        title.setForeground(UITheme.DEEP);

        JLabel description = new JLabel(module.description());
        description.setFont(UITheme.BODY);
        description.setForeground(UITheme.MUTED);

        JLabel tables = new JLabel(module.tables());
        tables.setFont(UITheme.MONO);
        tables.setForeground(UITheme.FOREST);

        JLabel tablesCaption = UITheme.hint("Reads and writes");
        tablesCaption.setForeground(UITheme.MUTED);

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.add(tablesCaption);
        meta.add(Box.createVerticalStrut(4));
        meta.add(tables);

        JButton launch = UITheme.primaryButton("Open " + module.title());
        launch.setPreferredSize(new Dimension(250, 42));
        launch.setAlignmentX(LEFT_ALIGNMENT);
        launch.addActionListener(e -> openModule(module));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(title);
        body.add(Box.createVerticalStrut(6));
        body.add(description);
        body.add(Box.createVerticalStrut(22));
        body.add(meta);
        body.add(Box.createVerticalStrut(24));
        body.add(launch);

        panel.add(body, BorderLayout.WEST);
        return panel;
    }

    private void selectNav(NavEntry selected) {

        NavEntry effective = selected == null ? overviewEntry : selected;
        for (NavEntry entry : navEntries) {
            boolean isSelected = entry == effective;
            entry.panel().putClientProperty("selected", isSelected);
            entry.panel().setBackground(isSelected ? UITheme.FOREST : UITheme.DEEP);
        }
    }

    private void openModule(Module module) {

        if (module == null || !module.built() || module.frameFactory() == null) return;
        JFrame frame = module.frameFactory().get();
        frame.setVisible(true);
    }

    private void openCurrentModule() {

        if (currentModule != null) openModule(currentModule);
    }
}
