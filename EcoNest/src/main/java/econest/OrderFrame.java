package econest.ui;

import econest.model.Client;
import econest.model.Order;
import econest.model.OrderItem;
import econest.model.Product;
import econest.service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderFrame extends JFrame {
    private final JComboBox<Client> clientBox = new JComboBox<>();
    private final JComboBox<Product> productBox = new JComboBox<>();
    private final JTextField quantityField = new JTextField("1", 5);
    private final JLabel totalLabel = new JLabel("Total: Rs. 0.00");
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Product", "Unit Price (Rs.)", "Quantity", "Subtotal (Rs.)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"Order ID", "Client", "Date / Time", "Items", "Total (Rs.)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final JTable historyTable = new JTable(historyModel);
    private final List<OrderItem> cart = new ArrayList<>();
    private final OrderService orderService = new OrderService();

    public OrderFrame() {
        setTitle("EcoNest - Task 5: Client Orders");
        setSize(900, 620);
        setMinimumSize(new Dimension(700, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Temporary sample data. Replace these with Task 1 and Task 2 data on integration.
        clientBox.addItem(new Client(1, "Nimal Perera (Household)"));
        clientBox.addItem(new Client(2, "Green Office (Office)"));
        productBox.addItem(new Product(1, "Aloe Vera Plant", 1500));
        productBox.addItem(new Product(2, "Clay Planter", 900));
        productBox.addItem(new Product(3, "Herb Garden Kit", 2500));

        JPanel top = new JPanel(new GridLayout(2, 1, 4, 4));
        JPanel clientRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clientRow.add(new JLabel("Client:"));
        clientRow.add(clientBox);
        top.add(clientRow);
        JPanel productRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productRow.add(new JLabel("Product:"));
        productRow.add(productBox);
        productRow.add(new JLabel("Quantity:"));
        productRow.add(quantityField);
        JButton addButton = new JButton("Add Item");
        productRow.add(addButton);
        top.add(productRow);

        JPanel cartPanel = new JPanel(new BorderLayout(6, 6));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("Remove Selected Item");
        JButton clearButton = new JButton("Clear Order");
        JButton createButton = new JButton("Create Order");
        cartActions.add(totalLabel);
        cartActions.add(removeButton);
        cartActions.add(clearButton);
        cartActions.add(createButton);
        cartPanel.add(cartActions, BorderLayout.SOUTH);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Order History (this session)"));
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        JButton detailsButton = new JButton("View Selected Order Details");
        JPanel historyActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        historyActions.add(detailsButton);
        historyPanel.add(historyActions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cartPanel, historyPanel);
        splitPane.setResizeWeight(0.55);
        add(top, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        applyEcoNestTheme(top, clientRow, productRow, cartPanel, cartActions, historyPanel, historyActions,
                addButton, removeButton, clearButton, createButton, detailsButton);

        addButton.addActionListener(e -> addItem());
        removeButton.addActionListener(e -> removeItem());
        clearButton.addActionListener(e -> clearCart());
        createButton.addActionListener(e -> createOrder());
        detailsButton.addActionListener(e -> viewDetails());
    }

    // Green EcoNest colour theme. This changes appearance only; order functionality is unchanged.
    private void applyEcoNestTheme(JPanel top, JPanel clientRow, JPanel productRow,
                                   JPanel cartPanel, JPanel cartActions,
                                   JPanel historyPanel, JPanel historyActions,
                                   JButton... buttons) {
        Color deepGreen = new Color(31, 91, 68);
        Color green = new Color(47, 115, 85);
        Color paleGreen = new Color(232, 244, 236);
        Color page = new Color(245, 249, 246);
        Color text = new Color(35, 70, 55);

        getContentPane().setBackground(page);
        top.setBackground(page);
        clientRow.setBackground(page);
        productRow.setBackground(page);
        cartPanel.setBackground(Color.WHITE);
        cartActions.setBackground(paleGreen);
        historyPanel.setBackground(Color.WHITE);
        historyActions.setBackground(paleGreen);

        cartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(174, 211, 187)),
                "Current Order", 0, 0, new Font("SansSerif", Font.BOLD, 14), deepGreen));
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(174, 211, 187)),
                "Order History (this session)", 0, 0, new Font("SansSerif", Font.BOLD, 14), deepGreen));

        for (Component component : new Component[]{clientRow, productRow}) {
            for (Component child : ((JPanel) component).getComponents()) {
                if (child instanceof JLabel) child.setForeground(text);
            }
        }
        totalLabel.setForeground(deepGreen);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        styleTable(cartTable, deepGreen, paleGreen, text);
        styleTable(historyTable, deepGreen, paleGreen, text);
        styleCombo(clientBox, paleGreen, text);
        styleCombo(productBox, paleGreen, text);
        styleTextField(quantityField, paleGreen, text);

        for (JButton button : buttons) {
            button.setFocusPainted(false);
            button.setBackground(green);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(deepGreen),
                    BorderFactory.createEmptyBorder(7, 13, 7, 13)));
            button.setOpaque(true);
        }
    }

    private void styleTable(JTable table, Color header, Color alternate, Color foreground) {
        table.setRowHeight(26);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(foreground);
        table.setGridColor(new Color(220, 234, 224));
        table.setSelectionBackground(new Color(190, 224, 201));
        table.setSelectionForeground(new Color(25, 65, 45));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setBackground(header);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
    }

    private void styleCombo(JComboBox<?> combo, Color background, Color foreground) {
        combo.setBackground(background);
        combo.setForeground(foreground);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void styleTextField(JTextField field, Color background, Color foreground) {
        field.setBackground(background);
        field.setForeground(foreground);
        field.setCaretColor(foreground);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(174, 211, 187)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
    }

    private void addItem() {
        Product product = (Product) productBox.getSelectedItem();
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a positive whole-number quantity.",
                    "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cart.add(new OrderItem(product, quantity));
        refreshCart();
    }

    private void removeItem() {
        int selected = cartTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to remove.");
            return;
        }
        cart.remove(cartTable.convertRowIndexToModel(selected));
        refreshCart();
    }

    private void clearCart() {
        cart.clear();
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        double total = 0;
        for (OrderItem item : cart) {
            cartModel.addRow(new Object[]{item.getProduct().getName(),
                    String.format("%.2f", item.getProduct().getPrice()),
                    item.getQuantity(), String.format("%.2f", item.getSubtotal())});
            total += item.getSubtotal();
        }
        totalLabel.setText(String.format("Total: Rs. %.2f", total));
    }

    private void createOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one product before creating an order.");
            return;
        }
        Client client = (Client) clientBox.getSelectedItem();
        Order order = orderService.createOrder(client, cart);
        historyModel.addRow(new Object[]{order.getOrderId(), client.getName(),
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                order.getItems().size(), String.format("%.2f", order.calculateTotal())});
        clearCart();
        JOptionPane.showMessageDialog(this, "Order #" + order.getOrderId()
                + " created successfully!\nTotal: Rs. " + String.format("%.2f", order.calculateTotal()));
    }

    private void viewDetails() {
        int selected = historyTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an order from history first.");
            return;
        }
        int row = historyTable.convertRowIndexToModel(selected);
        Order order = orderService.getOrderHistory().get(row);
        StringBuilder message = new StringBuilder("Order #" + order.getOrderId()
                + "\nClient: " + order.getClient().getName() + "\n\n");
        for (OrderItem item : order.getItems()) {
            message.append(item.getProduct().getName()).append(" x ")
                    .append(item.getQuantity()).append(" = Rs. ")
                    .append(String.format("%.2f", item.getSubtotal())).append("\n");
        }
        message.append("\nTotal: Rs. ").append(String.format("%.2f", order.calculateTotal()));
        JOptionPane.showMessageDialog(this, message.toString(), "Order Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
