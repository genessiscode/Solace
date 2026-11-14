package econest;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 5 - client orders. Reads clients (module 2), products and stock
 * (modules 1 and 3), writes orders and order items, and hands the reserved
 * quantities back to inventory when an order is cancelled.
 */
public class OrderFrame extends JFrame {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static String money(double value) {
        return String.format("%,.2f", value);
    }

    private final JComboBox<Client> clientBox = new JComboBox<>();
    private final JComboBox<Product> productBox = new JComboBox<>();
    private final JTextField quantityField = new JTextField("1", 5);
    private final JLabel stockHint = UITheme.hint("No product selected");
    private final JLabel totalLabel = new JLabel("Total: Rs. 0.00");

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Product", "Unit price (Rs.)", "Qty", "Subtotal (Rs.)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"Order", "Client", "Date", "Status", "Items", "Total (Rs.)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final JTable historyTable = new JTable(historyModel);

    private final StatCard ordersCard = new StatCard("Orders", UITheme.LEAF);
    private final StatCard openCard = new StatCard("Open orders", UITheme.MOSS);
    private final StatCard revenueCard = new StatCard("Order value", UITheme.FOREST);

    private final JLabel statusLabel = UITheme.hint(" ");

    private final List<OrderItem> cart = new ArrayList<>();
    private final List<Order> history = new ArrayList<>();
    private final Map<String, Integer> stock = new HashMap<>();
    private final OrderService orderService = new OrderService();

    private boolean loaded;

    public OrderFrame() {

        setTitle("EcoNest - Client Orders");
        setSize(1280, 820);
        setMinimumSize(new Dimension(1080, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(createContent());
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                if (loaded) {
                    refreshReferenceData();
                    refreshHistory();
                }
            }
        });

        refreshReferenceData();
        refreshHistory();
        loaded = true;
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UITheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        root.add(createTopPanel(), BorderLayout.NORTH);
        root.add(createBodyPanel(), BorderLayout.CENTER);
        return root;
    }

    private JPanel createTopPanel() {

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);

        top.add(new GreenHeader(
                "Client Orders",
                "Build an order from the shared client and product tables - stock moves with it."
        ), BorderLayout.NORTH);

        ordersCard.setHint("All recorded orders");
        openCard.setHint("Pending or confirmed");
        revenueCard.setHint("Excluding cancelled");

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(ordersCard);
        stats.add(openCard);
        stats.add(revenueCard);

        top.add(stats, BorderLayout.CENTER);
        return top;
    }

    private JPanel createBodyPanel() {

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(createOrderCard(), BorderLayout.WEST);
        body.add(createHistoryCard(), BorderLayout.CENTER);
        return body;
    }

    private RoundedPanel createOrderCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(440, 0));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        heading.add(UITheme.sectionTitle("New order"));
        heading.add(UITheme.hint("Pick a client, add products, then commit the order."));

        JPanel fields = new JPanel(new GridLayout(3, 1, 0, 8));
        fields.setOpaque(false);

        UITheme.styleCombo(clientBox);
        UITheme.styleCombo(productBox);
        UITheme.styleField(quantityField);

        fields.add(fieldGroup("Client", clientBox));
        fields.add(fieldGroup("Product", productBox));

        JPanel quantityRow = new JPanel(new BorderLayout(8, 0));
        quantityRow.setOpaque(false);
        quantityRow.add(quantityField, BorderLayout.WEST);
        JButton addButton = UITheme.primaryButton("Add item");
        quantityRow.add(addButton, BorderLayout.CENTER);

        JPanel quantityGroup = new JPanel(new BorderLayout(0, 4));
        quantityGroup.setOpaque(false);
        quantityGroup.add(UITheme.fieldLabel("Quantity"), BorderLayout.NORTH);
        quantityGroup.add(quantityRow, BorderLayout.CENTER);
        quantityGroup.add(stockHint, BorderLayout.SOUTH);
        fields.add(quantityGroup);

        cartTable.setModel(cartModel);
        UITheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(2).setMaxWidth(60);
        JScrollPane cartScroll = UITheme.scroll(cartTable);

        JPanel cartBlock = new JPanel(new BorderLayout(0, 8));
        cartBlock.setOpaque(false);
        cartBlock.add(UITheme.fieldLabel("Items in this order"), BorderLayout.NORTH);
        cartBlock.add(cartScroll, BorderLayout.CENTER);

        totalLabel.setFont(UITheme.BODY_BOLD);
        totalLabel.setForeground(UITheme.DEEP);

        JButton removeButton = UITheme.ghostButton("Remove line");
        JButton clearButton = UITheme.ghostButton("Clear");

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cartActions.setOpaque(false);
        cartActions.add(totalLabel);
        cartActions.add(removeButton);
        cartActions.add(clearButton);
        cartBlock.add(cartActions, BorderLayout.SOUTH);

        JButton createButton = UITheme.primaryButton("Create order");
        createButton.setPreferredSize(new Dimension(0, 40));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        createButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(fields);
        form.add(Box.createVerticalStrut(12));
        form.add(cartBlock);

        card.add(heading, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(createButton, BorderLayout.SOUTH);

        productBox.addActionListener(e -> updateStockHint());
        addButton.addActionListener(e -> addItem());
        removeButton.addActionListener(e -> removeItem());
        clearButton.addActionListener(e -> {
            clearCart();
            setStatus("Order cleared.");
        });
        createButton.addActionListener(e -> createOrder());

        return card;
    }

    private RoundedPanel createHistoryCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 3));
        titles.setOpaque(false);
        titles.add(UITheme.sectionTitle("Order history"));
        titles.add(UITheme.hint("Newest first - status follows the delivery module."));
        heading.add(titles, BorderLayout.WEST);

        JButton refreshButton = UITheme.secondaryButton("Refresh");
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        tools.setOpaque(false);
        tools.add(refreshButton);
        heading.add(tools, BorderLayout.EAST);

        UITheme.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(3)
                .setCellRenderer(UITheme.statusPillRenderer());
        JScrollPane historyScroll = UITheme.scroll(historyTable);

        JButton detailsButton = UITheme.secondaryButton("View details");
        JButton cancelButton = UITheme.dangerButton("Cancel order");

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.add(statusLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(detailsButton);
        actions.add(cancelButton);
        footer.add(actions, BorderLayout.EAST);

        card.add(heading, BorderLayout.NORTH);
        card.add(historyScroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshHistory());
        detailsButton.addActionListener(e -> viewDetails());
        cancelButton.addActionListener(e -> cancelSelectedOrder());

        return card;
    }

    private static JPanel fieldGroup(String label, Component input) {

        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.add(UITheme.fieldLabel(label), BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
    }

    // ================= DATA =================

    private void refreshReferenceData() {

        String clientId = selectedClientId();
        String productId = selectedProductId();

        try {
            List<Client> clients = orderService.loadClients();
            clientBox.removeAllItems();
            for (Client client : clients) clientBox.addItem(client);
            selectClient(clientId);

            List<Product> products = orderService.loadProducts();
            productBox.removeAllItems();
            for (Product product : products) productBox.addItem(product);
            selectProduct(productId);

            stock.clear();
            stock.putAll(orderService.loadStockLevels());
            updateStockHint();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void refreshHistory() {

        try {
            history.clear();
            history.addAll(orderService.getOrderHistory());
        } catch (SQLException ex) {
            showDatabaseError(ex);
            return;
        }

        String selectedOrder = selectedHistoryOrderId();
        historyModel.setRowCount(0);
        double value = 0;
        int open = 0;
        for (Order order : history) {
            historyModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getClient().getName(),
                    order.getCreatedAt().format(DATE),
                    order.getStatus(),
                    order.getItems().size(),
                    money(order.calculateTotal())});
            if (order.isOpen()) open++;
            if (!order.isCancelled()) value += order.calculateTotal();
        }

        ordersCard.setValue(history.size());
        openCard.setValue(open);
        revenueCard.setValue("Rs. " + String.format("%,.0f", value));

        selectHistoryOrder(selectedOrder);
    }

    private String selectedClientId() {
        Client client = (Client) clientBox.getSelectedItem();
        return client == null ? null : client.getId();
    }

    private String selectedProductId() {
        Product product = (Product) productBox.getSelectedItem();
        return product == null ? null : product.getProductId();
    }

    private void selectClient(String clientId) {
        if (clientId == null) return;
        for (int i = 0; i < clientBox.getItemCount(); i++) {
            if (clientId.equals(clientBox.getItemAt(i).getId())) {
                clientBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectProduct(String productId) {
        if (productId == null) return;
        for (int i = 0; i < productBox.getItemCount(); i++) {
            if (productId.equals(productBox.getItemAt(i).getProductId())) {
                productBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private String selectedHistoryOrderId() {
        int row = historyTable.getSelectedRow();
        if (row < 0) return null;
        int modelRow = historyTable.convertRowIndexToModel(row);
        return modelRow < historyModel.getRowCount()
                ? String.valueOf(historyModel.getValueAt(modelRow, 0))
                : null;
    }

    private void selectHistoryOrder(String orderId) {
        if (orderId == null) return;
        for (int row = 0; row < historyModel.getRowCount(); row++) {
            if (orderId.equals(historyModel.getValueAt(row, 0))) {
                int viewRow = historyTable.convertRowIndexToView(row);
                if (viewRow >= 0) historyTable.setRowSelectionInterval(viewRow, viewRow);
                return;
            }
        }
    }

    private void updateStockHint() {

        Product product = (Product) productBox.getSelectedItem();
        if (product == null) {
            stockHint.setText("No product selected");
            return;
        }

        Integer available = stock.get(product.getProductId());
        int inCart = cartQuantity(product.getProductId());
        if (available == null) {
            stockHint.setText(product.getName() + " - no stock record");
        } else {
            stockHint.setText(product.getName() + " - " + available
                    + " in stock, " + inCart + " in this order");
        }
    }

    private int cartQuantity(String productId) {
        int total = 0;
        for (OrderItem item : cart) {
            if (productId.equals(item.getProduct().getProductId())) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    // ================= CART =================

    private void addItem() {

        Product product = (Product) productBox.getSelectedItem();
        if (product == null) {
            setStatus("Choose a product first.");
            return;
        }

        int quantity = parseQuantity();
        if (quantity <= 0) return;

        Integer available = stock.get(product.getProductId());
        if (available == null) {
            setStatus("No stock record for " + product.getName() + ".");
            return;
        }

        int requested = cartQuantity(product.getProductId()) + quantity;
        if (requested > available) {
            setStatus("Not enough stock for " + product.getName()
                    + " - available " + available + ", requested " + requested + ".");
            return;
        }

        cart.add(new OrderItem(product, quantity));
        refreshCart();
        updateStockHint();
        setStatus(product.getName() + " x " + quantity + " added.");
    }

    private int parseQuantity() {
        String text = quantityField.getText().trim();
        try {
            int quantity = Integer.parseInt(text);
            if (quantity <= 0 || quantity > 9999) throw new NumberFormatException();
            return quantity;
        } catch (NumberFormatException ex) {
            setStatus("Enter a positive whole-number quantity (1-9999).");
            return -1;
        }
    }

    private void removeItem() {

        int selected = cartTable.getSelectedRow();
        if (selected < 0) {
            setStatus("Select a line in the order first.");
            return;
        }
        cart.remove(cartTable.convertRowIndexToModel(selected));
        refreshCart();
        updateStockHint();
        setStatus("Line removed.");
    }

    private void clearCart() {
        cart.clear();
        refreshCart();
        updateStockHint();
    }

    private void refreshCart() {

        cartModel.setRowCount(0);
        double total = 0;
        for (OrderItem item : cart) {
            cartModel.addRow(new Object[]{
                    item.getProduct().getName(),
                    money(item.getProduct().getPrice()),
                    item.getQuantity(),
                    money(item.getSubtotal())});
            total += item.getSubtotal();
        }
        totalLabel.setText("Total: Rs. " + money(total));
    }

    // ================= ACTIONS =================

    private void createOrder() {

        if (cart.isEmpty()) {
            setStatus("Add at least one product before creating an order.");
            return;
        }

        Client client = (Client) clientBox.getSelectedItem();
        if (client == null) {
            setStatus("Choose a client first.");
            return;
        }

        try {
            Order order = orderService.createOrder(client, cart);
            clearCart();
            refreshReferenceData();
            refreshHistory();
            selectHistoryOrder(order.getOrderId());
            setStatus("Order " + order.getOrderId() + " created - stock updated.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            setStatus(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Order not saved", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void cancelSelectedOrder() {

        int selected = historyTable.getSelectedRow();
        if (selected < 0) {
            setStatus("Select an order in the history first.");
            return;
        }

        int modelRow = historyTable.convertRowIndexToModel(selected);
        if (modelRow < 0 || modelRow >= history.size()) return;
        Order order = history.get(modelRow);

        if (!order.isOpen()) {
            setStatus("Order " + order.getOrderId() + " is "
                    + order.getStatus() + " and cannot be cancelled.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel order " + order.getOrderId() + " for "
                        + order.getClient().getName()
                        + "?\nIts quantities return to stock.",
                "Cancel order", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            boolean cancelled = orderService.cancelOrder(order.getOrderId());
            refreshReferenceData();
            refreshHistory();
            setStatus(cancelled
                    ? "Order " + order.getOrderId() + " cancelled - stock restored."
                    : "Order " + order.getOrderId() + " was already cancelled.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            setStatus(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Order not cancelled", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void viewDetails() {

        int selected = historyTable.getSelectedRow();
        if (selected < 0) {
            setStatus("Select an order in the history first.");
            return;
        }

        int modelRow = historyTable.convertRowIndexToModel(selected);
        if (modelRow < 0 || modelRow >= history.size()) return;
        Order order = history.get(modelRow);

        StringBuilder message = new StringBuilder("Order #" + order.getOrderId()
                + "\nClient: " + order.getClient().getName()
                + "\nDate: " + order.getCreatedAt().format(DATE)
                + "\nStatus: " + order.getStatus() + "\n\n");
        for (OrderItem item : order.getItems()) {
            message.append(item.getProduct().getName()).append(" x ")
                    .append(item.getQuantity()).append(" = Rs. ")
                    .append(money(item.getSubtotal())).append("\n");
        }
        message.append("\nTotal: Rs. ").append(money(order.calculateTotal()));

        JOptionPane.showMessageDialog(this, message.toString(),
                "Order details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    private void showDatabaseError(SQLException ex) {
        setStatus("Database error - check config.properties.");
        JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage()
                        + "\nCheck config.properties and the MariaDB connection.",
                "Database unavailable", JOptionPane.ERROR_MESSAGE);
    }
}
