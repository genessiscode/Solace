package econest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The client orders module: create orders from a draft of lines, move
 * them through their statuses and remove them.
 *
 * <p>Follows the same visual structure as {@link InventoryFrame}: form on
 * top ({@link BorderLayout#NORTH}), table in the middle
 * ({@link BorderLayout#CENTER}) and buttons at the bottom
 * ({@link BorderLayout#SOUTH}). Because an order is made of lines, the
 * middle holds two stacked tables - the saved orders, and the items of
 * the selected order - plus a draft table fed by the "add line" strip.
 * Lines are collected in the draft first and only written to the database
 * when <em>Create Order</em> runs, in one transaction.</p>
 *
 * <p>The frame closes with {@code DISPOSE_ON_CLOSE} because the dashboard
 * opens it and closing it must return there, not terminate the application.
 * Model validation errors ({@link IllegalArgumentException}) and database
 * errors ({@link IllegalStateException}) are shown in a
 * {@link JOptionPane} - no SQL failure is ever allowed to reach the
 * console.</p>
 */
public class OrderFrame extends JFrame {

    private JTextField orderIdField;
    private JComboBox<String> clientCombo;
    private JTextField orderDateField;
    private JComboBox<OrderStatus> statusCombo;
    private JComboBox<String> productCombo;
    private JTextField quantityField;

    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JTable draftTable;
    private DefaultTableModel draftTableModel;

    private JLabel orderTotalLabel;
    private JLabel draftTotalLabel;

    private OrderManager orderManager;

    private ArrayList<Order> orderList = new ArrayList<>();

    private ArrayList<String[]> clientOptions = new ArrayList<>();

    private ArrayList<String[]> productOptions = new ArrayList<>();

    private final ArrayList<OrderItem> draftItems = new ArrayList<>();

    /**
     * Builds the window, loads the order list and shows the frame.
     */
    public OrderFrame() {

        orderManager = new OrderManager();

        setTitle("EcoNest - Client Orders");
        setSize(1000, 700);
        // DISPOSE_ON_CLOSE, not EXIT_ON_CLOSE: the dashboard opens this frame
        // and closing it must return there, not terminate the application.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        createGUI();

        updateTable();

        setVisible(true);
    }

    // ================= CREATE GUI =================

    private void createGUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10
                )
        );

        // ================= INPUT PANEL =================

        JPanel inputPanel = new JPanel(
                new GridLayout(2, 4, 10, 10)
        );

        inputPanel.add(new JLabel("Order ID:"));

        orderIdField = new JTextField();
        inputPanel.add(orderIdField);

        inputPanel.add(new JLabel("Client:"));

        clientCombo = new JComboBox<>();
        inputPanel.add(clientCombo);

        inputPanel.add(new JLabel("Order Date (yyyy-MM-dd):"));

        orderDateField = new JTextField();
        orderDateField.setText(LocalDate.now().toString());
        inputPanel.add(orderDateField);

        inputPanel.add(new JLabel("Status:"));

        statusCombo = new JComboBox<>(OrderStatus.values());
        inputPanel.add(statusCombo);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // ================= TABLES =================

        ordersTableModel = new DefaultTableModel(
                new String[]{
                        "Order ID",
                        "Client",
                        "Date",
                        "Status",
                        "Total"
                },
                0
        );

        ordersTable = new JTable(ordersTableModel);

        itemsTableModel = new DefaultTableModel(
                new String[]{
                        "Product ID",
                        "Product",
                        "Qty",
                        "Unit Price",
                        "Line Total"
                },
                0
        );

        itemsTable = new JTable(itemsTableModel);

        draftTableModel = new DefaultTableModel(
                new String[]{
                        "Product ID",
                        "Product",
                        "Qty",
                        "Unit Price",
                        "Line Total"
                },
                0
        );

        draftTable = new JTable(draftTableModel);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        centerPanel.add(
                new JScrollPane(ordersTable),
                BorderLayout.NORTH
        );

        // The saved orders sit on top; the items of the selected order
        // and the draft share the space below.
        JPanel lowerPanel = new JPanel(
                new GridLayout(2, 1, 10, 10)
        );

        lowerPanel.add(createItemsPanel());
        lowerPanel.add(createDraftPanel());

        centerPanel.add(lowerPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ================= BUTTON PANEL =================

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 5, 10, 10)
        );

        JButton createOrderButton =
                new JButton("Create Order");

        JButton updateStatusButton =
                new JButton("Update Status");

        JButton removeOrderButton =
                new JButton("Remove Order");

        JButton clearButton =
                new JButton("Clear");

        JButton refreshButton =
                new JButton("Refresh");

        buttonPanel.add(createOrderButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(removeOrderButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        // ================= PICKER OPTIONS =================

        loadPickerOptions();

        // ================= ORDER ROW CLICK =================

        ordersTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int row =
                                ordersTable.getSelectedRow();

                        if (row >= 0 && row < orderList.size()) {

                            Order order =
                                    orderList.get(row);

                            orderIdField.setText(
                                    order.getOrderId()
                            );

                            selectClient(
                                    order.getClientId()
                            );

                            orderDateField.setText(
                                    order.getOrderDate().toString()
                            );

                            statusCombo.setSelectedItem(
                                    order.getStatus()
                            );

                            loadItems(order);
                        }
                    }
                });

        // ================= BUTTON ACTIONS =================

        createOrderButton.addActionListener(e -> createOrder());

        updateStatusButton.addActionListener(e -> updateStatus());

        removeOrderButton.addActionListener(e -> removeOrder());

        clearButton.addActionListener(e -> clearFields());

        refreshButton.addActionListener(e -> updateTable());
    }

    // ================= PANEL BUILDERS =================

    /** The saved items of the selected order, with their total. */
    private JPanel createItemsPanel() {

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.add(
                new JLabel("Order Items (selected order):"),
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(itemsTable),
                BorderLayout.CENTER
        );

        orderTotalLabel = new JLabel("Order Total: Rs. 0.00");

        panel.add(orderTotalLabel, BorderLayout.SOUTH);

        return panel;
    }

    /** The "add line" strip, the draft table and the draft total. */
    private JPanel createDraftPanel() {

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel strip = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        strip.add(new JLabel("Product:"));

        productCombo = new JComboBox<>();
        strip.add(productCombo);

        strip.add(new JLabel("Qty:"));

        quantityField = new JTextField(5);
        strip.add(quantityField);

        JButton addLineButton = new JButton("Add Line");
        strip.add(addLineButton);

        JButton removeLineButton = new JButton("Remove Line");
        strip.add(removeLineButton);

        panel.add(strip, BorderLayout.NORTH);

        panel.add(
                new JScrollPane(draftTable),
                BorderLayout.CENTER
        );

        draftTotalLabel = new JLabel("Draft Total: Rs. 0.00");

        panel.add(draftTotalLabel, BorderLayout.SOUTH);

        // ================= ADD LINE =================

        addLineButton.addActionListener(e -> {

            int productIndex =
                    productCombo.getSelectedIndex();

            if (productIndex < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a product."
                );

                return;
            }

            String quantityText =
                    quantityField.getText().trim();

            if (quantityText.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a quantity."
                );

                return;
            }

            try {

                int quantity =
                        Integer.parseInt(quantityText);

                if (quantity <= 0) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Quantity must be at least 1."
                    );

                    return;
                }

                String[] option =
                        productOptions.get(productIndex);

                double unitPrice =
                        Double.parseDouble(option[2]);

                OrderItem item = new OrderItem(
                        option[0],
                        option[1],
                        quantity,
                        unitPrice
                );

                draftItems.add(item);

                refreshDraftTable();

                quantityField.setText("");

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid quantity."
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage()
                );
            }
        });

        // ================= REMOVE LINE =================

        removeLineButton.addActionListener(e -> {

            int row =
                    draftTable.getSelectedRow();

            if (row < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a draft line to remove."
                );

                return;
            }

            draftItems.remove(row);

            refreshDraftTable();
        });

        return panel;
    }

    // ================= PICKER OPTIONS =================

    /**
     * Fills the client and product combos from their own SQL against the
     * clients and products tables, storing the raw columns alongside so a
     * selection can be turned back into an id and a price.
     */
    private void loadPickerOptions() {

        try {

            clientOptions =
                    orderManager.getClientOptions();

            clientCombo.removeAllItems();

            for (String[] client : clientOptions) {

                clientCombo.addItem(
                        client[0] + " - " + client[1]
                );
            }

            productOptions =
                    orderManager.getProductOptions();

            productCombo.removeAllItems();

            for (String[] product : productOptions) {

                productCombo.addItem(
                        String.format(
                                Locale.ROOT,
                                "%s - %s (%.2f)",
                                product[0],
                                product[1],
                                Double.parseDouble(product[2])
                        )
                );
            }

        } catch (IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    /** Selects the combo entry whose stored client id matches. */
    private void selectClient(String clientId) {

        for (int i = 0; i < clientOptions.size(); i++) {

            if (clientOptions.get(i)[0].equals(clientId)) {

                clientCombo.setSelectedIndex(i);

                return;
            }
        }
    }

    // ================= CREATE ORDER =================

    /**
     * Validates the form and the draft, then writes the order and its
     * lines in one transaction.
     */
    private void createOrder() {

        try {

            String orderId =
                    orderIdField.getText().trim();

            if (orderId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter an Order ID."
                );

                return;
            }

            if (isDuplicateOrder(orderId)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Order ID already exists."
                );

                return;
            }

            int clientIndex =
                    clientCombo.getSelectedIndex();

            if (clientIndex < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a client."
                );

                return;
            }

            if (draftItems.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please add at least one order line."
                );

                return;
            }

            LocalDate orderDate;

            try {

                orderDate = LocalDate.parse(
                        orderDateField.getText().trim()
                );

            } catch (DateTimeParseException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid date in yyyy-MM-dd format."
                );

                return;
            }

            String[] client =
                    clientOptions.get(clientIndex);

            Order order = new Order(
                    orderId,
                    client[0],
                    client[1],
                    orderDate,
                    (OrderStatus) statusCombo.getSelectedItem(),
                    0.0,
                    draftItems
            );

            orderManager.createOrder(order);

            JOptionPane.showMessageDialog(
                    this,
                    "Order Created Successfully!\n"
                            + "Order ID: " + orderId + "\n"
                            + "Total: Rs. "
                            + String.format(
                                    Locale.ROOT,
                                    "%.2f",
                                    order.getTotalAmount())
            );

            draftItems.clear();
            refreshDraftTable();
            updateTable();
            clearFields();

        } catch (IllegalArgumentException | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    /**
     * Asks the database whether the id is already taken, so the user is
     * told before any insert is attempted.
     */
    private boolean isDuplicateOrder(String orderId) {

        for (Order order : orderManager.getOrderList()) {

            if (order.getOrderId().equals(orderId)) {
                return true;
            }
        }

        return false;
    }

    // ================= UPDATE STATUS =================

    /** Moves the order shown in the form to the status in the combo. */
    private void updateStatus() {

        String orderId =
                orderIdField.getText().trim();

        if (orderId.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select or enter an Order ID."
            );

            return;
        }

        try {

            orderManager.updateOrderStatus(
                    orderId,
                    (OrderStatus) statusCombo.getSelectedItem()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Order Status Updated!"
            );

            updateTable();
            clearFields();

        } catch (IllegalArgumentException | IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    // ================= REMOVE ORDER =================

    /** Confirms, then deletes the order shown in the form. */
    private void removeOrder() {

        String orderId =
                orderIdField.getText().trim();

        if (orderId.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select or enter an Order ID."
            );

            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to remove this order?",
                        "Confirm Remove",
                        JOptionPane.YES_NO_OPTION
                );

        if (confirm
                == JOptionPane.YES_OPTION) {

            try {

                boolean removed =
                        orderManager.removeOrder(
                                orderId
                        );

                if (removed) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Order Removed Successfully!"
                    );

                    updateTable();
                    clearFields();

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Order not found."
                    );
                }

            } catch (IllegalStateException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage()
                );
            }
        }
    }

    // ================= ITEM TABLE =================

    /**
     * Loads the saved lines of the order into the items table and shows
     * its total.
     *
     * @param order the order whose items to show
     */
    private void loadItems(Order order) {

        itemsTableModel.setRowCount(0);

        try {

            ArrayList<OrderItem> items =
                    orderManager.getOrderItems(
                            order.getOrderId()
                    );

            for (OrderItem item : items) {

                itemsTableModel.addRow(
                        new Object[]{
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()
                        }
                );
            }

            orderTotalLabel.setText(
                    String.format(
                            Locale.ROOT,
                            "Order Total: Rs. %.2f",
                            order.getTotalAmount()
                    )
            );

        } catch (IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    // ================= DRAFT TABLE =================

    /** Rebuilds the draft table from the draft list and its total. */
    private void refreshDraftTable() {

        draftTableModel.setRowCount(0);

        double total = 0.0;

        for (OrderItem item : draftItems) {

            draftTableModel.addRow(
                    new Object[]{
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getLineTotal()
                    }
            );

            total += item.getLineTotal();
        }

        draftTotalLabel.setText(
                String.format(
                        Locale.ROOT,
                        "Draft Total: Rs. %.2f",
                        total
                )
        );
    }

    // ================= UPDATE TABLE =================

    private void updateTable() {

        try {

            orderList =
                    orderManager.getOrderList();

        } catch (IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );

            return;
        }

        ordersTableModel.setRowCount(0);

        for (Order order : orderList) {

            ordersTableModel.addRow(
                    new Object[]{
                            order.getOrderId(),
                            order.getClientName(),
                            order.getOrderDate().toString(),
                            order.getStatus(),
                            order.getTotalAmount()
                    }
            );
        }
    }

    // ================= CLEAR FIELDS =================

    private void clearFields() {

        orderIdField.setText("");
        orderDateField.setText(LocalDate.now().toString());
        statusCombo.setSelectedIndex(0);

        if (!clientOptions.isEmpty()) {
            clientCombo.setSelectedIndex(0);
        }

        itemsTableModel.setRowCount(0);
        orderTotalLabel.setText("Order Total: Rs. 0.00");

        ordersTable.clearSelection();

        // The draft is deliberately kept: it survives until the order is
        // created (or its lines are removed one by one), so clearing the
        // form never loses work in progress.
    }
}
