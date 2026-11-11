package econest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Stock and inventory management: quantities on hand, reorder levels,
 * suppliers, stock movements and low-stock alerts.
 */
public class InventoryFrame extends JFrame {

    private final InventoryManager inventoryManager = new InventoryManager();

    private final JComboBox<InventoryManager.ProductChoice> productCombo =
            new JComboBox<>();
    private final JTextField quantityField = new JTextField();
    private final JTextField reorderField = new JTextField();
    private final JTextField supplierField = new JTextField();
    private final JTextField stockAmountField = new JTextField();
    private final JTextField searchField = new JTextField(16);

    private final StatCard trackedCard =
            new StatCard("Products tracked", UITheme.FOREST);
    private final StatCard unitsCard =
            new StatCard("Units on hand", UITheme.LEAF);
    private final StatCard lowStockCard =
            new StatCard("Low stock", UITheme.WARNING);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                    "Product ID", "Product", "On Hand", "Reorder",
                    "Supplier", "Status"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    private boolean lowStockOnly;

    public InventoryFrame() {

        setTitle("EcoNest - Stock / Inventory (Task 3)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);

        buildUI();
        loadProductChoices();
        refreshAll();

        setVisible(true);
    }

    // ================= GUI =================

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(18, 20, 18, 20));
        root.setBackground(UITheme.BG);
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 16));
        header.setOpaque(false);
        header.add(new GreenHeader("Stock & Inventory",
                        "Track quantities, reorder levels, suppliers and"
                                + " low-stock alerts."),
                BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.add(trackedCard);
        summary.add(unitsCard);
        summary.add(lowStockCard);
        header.add(summary, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createFormCard(), BorderLayout.NORTH);
        content.add(createTableCard(), BorderLayout.CENTER);
        content.add(createActionBar(), BorderLayout.SOUTH);
        root.add(content, BorderLayout.CENTER);
    }

    private JPanel createFormCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(UITheme.sectionTitle("Stock record"), BorderLayout.WEST);
        title.add(UITheme.hint(
                        "Pick a catalogue product, then set its quantity,"
                                + " reorder level and supplier."),
                BorderLayout.SOUTH);
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(fields, gbc, 0, "PRODUCT", productCombo);
        addField(fields, gbc, 1, "QUANTITY ON HAND", quantityField);
        addField(fields, gbc, 2, "REORDER LEVEL", reorderField);
        addField(fields, gbc, 3, "SUPPLIER", supplierField);

        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int column,
            String label,
            JComponent input) {

        int x = column * 2;

        gbc.gridx = x;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(UITheme.fieldLabel(label), gbc);

        if (input instanceof JTextField) {
            UITheme.styleField((JTextField) input);
        } else if (input instanceof JComboBox) {
            UITheme.styleCombo((JComboBox<?>) input);
        }

        input.setPreferredSize(new Dimension(180, 40));

        gbc.gridx = x + 1;
        gbc.weightx = 1;
        panel.add(input, gbc);
    }

    private JPanel createTableCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(UITheme.sectionTitle("Inventory"), BorderLayout.WEST);
        title.add(UITheme.hint("Select a row to load it into the form."),
                BorderLayout.SOUTH);
        card.add(title, BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(UITheme.fieldLabel("SEARCH"));
        UITheme.styleField(searchField);
        searchField.setPreferredSize(new Dimension(220, 38));
        searchBar.add(searchField);

        JButton searchButton = UITheme.secondaryButton("Search");
        searchButton.addActionListener(e -> refreshTable());
        searchBar.add(searchButton);

        JButton showAllButton = UITheme.ghostButton("Show All");
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            lowStockOnly = false;
            refreshTable();
        });
        searchBar.add(showAllButton);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(5)
                .setCellRenderer(UITheme.statusPillRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    loadSelectedRow(table.convertRowIndexToModel(row));
                }
            }
        });

        JScrollPane scroll = UITheme.scroll(table);
        scroll.setPreferredSize(new Dimension(900, 260));

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(searchBar, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActionBar() {

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        JPanel stockBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stockBar.setOpaque(false);
        stockBar.add(UITheme.fieldLabel("STOCK AMOUNT"));
        UITheme.styleField(stockAmountField);
        stockAmountField.setPreferredSize(new Dimension(90, 38));
        stockBar.add(stockAmountField);

        JButton stockInButton = UITheme.primaryButton("Stock In  +");
        stockInButton.addActionListener(e -> stockIn());
        stockBar.add(stockInButton);

        JButton stockOutButton = UITheme.dangerButton("Stock Out  −");
        stockOutButton.addActionListener(e -> stockOut());
        stockBar.add(stockOutButton);

        JButton checkButton = UITheme.secondaryButton("Check");
        checkButton.addActionListener(e -> checkStock());
        stockBar.add(checkButton);

        JPanel recordBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        recordBar.setOpaque(false);

        JButton addButton = UITheme.secondaryButton("Add Record");
        addButton.addActionListener(e -> addInventory());
        recordBar.add(addButton);

        JButton updateButton = UITheme.secondaryButton("Update Record");
        updateButton.addActionListener(e -> updateInventory());
        recordBar.add(updateButton);

        JButton removeButton = UITheme.dangerButton("Remove Record");
        removeButton.addActionListener(e -> removeInventory());
        recordBar.add(removeButton);

        JButton lowStockButton = UITheme.ghostButton("Low Stock");
        lowStockButton.addActionListener(e -> {
            lowStockOnly = true;
            refreshTable();
        });
        recordBar.add(lowStockButton);

        JButton refreshButton = UITheme.secondaryButton("↻ Refresh");
        refreshButton.addActionListener(e -> {
            loadProductChoices();
            refreshAll();
        });
        recordBar.add(refreshButton);

        bar.add(stockBar, BorderLayout.WEST);
        bar.add(recordBar, BorderLayout.EAST);
        return bar;
    }

    // ================= ACTIONS =================

    private void addInventory() {

        try {
            Inventory item = buildInventory();

            if (inventoryManager.findProduct(item.getProductId()) != null) {
                showError("Inventory already exists for product "
                        + item.getProductId() + ". Use Update Record.");
                return;
            }

            inventoryManager.addInventory(item);
            showInfo("Stock record added successfully.");
            refreshAll();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateInventory() {

        try {
            Inventory item = buildInventory();
            inventoryManager.updateInventory(item.getProductId(), item);
            showInfo("Stock record updated successfully.");
            refreshAll();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeInventory() {

        InventoryManager.ProductChoice choice = selectedProduct();

        if (choice == null) {
            showError("Select a product first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove the inventory record for "
                        + choice.getProductId() + " — "
                        + choice.getProductName() + "?\n"
                        + "The catalogue product itself is kept.",
                "Confirm remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (inventoryManager.removeInventory(choice.getProductId())) {
                showInfo("Stock record removed.");
                refreshAll();
            } else {
                showError("No inventory record was found for that product.");
            }

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void stockIn() {

        InventoryManager.ProductChoice choice = selectedProduct();

        if (choice == null) {
            showError("Select a product first.");
            return;
        }

        try {
            int amount = parsePositiveAmount();
            String supplier = supplierField.getText().trim();

            inventoryManager.addStock(
                    choice.getProductId(), amount, supplier);

            showInfo("Added " + amount + " units to "
                    + choice.getProductName() + ".");
            stockAmountField.setText("");
            refreshAll();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void stockOut() {

        InventoryManager.ProductChoice choice = selectedProduct();

        if (choice == null) {
            showError("Select a product first.");
            return;
        }

        try {
            int amount = parsePositiveAmount();
            inventoryManager.removeStock(choice.getProductId(), amount);

            showInfo("Removed " + amount + " units from "
                    + choice.getProductName() + ".");
            stockAmountField.setText("");
            refreshAll();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void checkStock() {

        InventoryManager.ProductChoice choice = selectedProduct();

        if (choice == null) {
            showError("Select a product first.");
            return;
        }

        try {
            Inventory item = inventoryManager.findProduct(
                    choice.getProductId());

            if (item == null) {
                showError("No inventory record for that product.");
                return;
            }

            String status = item.isLowStock()
                    ? "LOW STOCK — reorder " + item.getStockGap() + " more"
                    : "Stock is OK";

            JOptionPane.showMessageDialog(
                    this,
                    "Product: " + item.getProductName()
                            + "\nOn hand: " + item.getQuantity()
                            + "\nReorder level: " + item.getReorderLevel()
                            + "\nSupplier: "
                            + (item.getSupplierName().isEmpty()
                                    ? "—"
                                    : item.getSupplierName())
                            + "\nStatus: " + status,
                    "Stock check",
                    item.isLowStock()
                            ? JOptionPane.WARNING_MESSAGE
                            : JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAll() {
        refreshTable();
        updateSummary();
    }

    private void refreshTable() {

        tableModel.setRowCount(0);

        try {
            String query = searchField.getText().trim().toLowerCase(
                    Locale.ROOT);

            ArrayList<Inventory> items = lowStockOnly
                    ? inventoryManager.getLowStockItems()
                    : inventoryManager.getInventoryList();

            for (Inventory item : items) {

                if (!query.isEmpty() && !matches(item, query)) {
                    continue;
                }

                tableModel.addRow(new Object[]{
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getReorderLevel(),
                        item.getSupplierName(),
                        item.isLowStock() ? "LOW" : "OK"
                });
            }

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean matches(Inventory item, String query) {
        return contains(item.getProductId(), query)
                || contains(item.getProductName(), query)
                || contains(item.getSupplierName(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void updateSummary() {

        try {
            ArrayList<Inventory> items = inventoryManager.getInventoryList();

            int units = 0;
            int low = 0;

            for (Inventory item : items) {
                units += item.getQuantity();
                if (item.isLowStock()) {
                    low++;
                }
            }

            trackedCard.setValue(items.size());
            unitsCard.setValue(units);
            lowStockCard.setValue(low);

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadProductChoices() {

        Object selected = productCombo.getSelectedItem();
        String selectedId = selected instanceof InventoryManager.ProductChoice
                ? ((InventoryManager.ProductChoice) selected).getProductId()
                : null;

        productCombo.removeAllItems();

        for (InventoryManager.ProductChoice choice
                : inventoryManager.getProductChoices()) {
            productCombo.addItem(choice);
        }

        if (selectedId != null) {
            selectProduct(selectedId);
        }
    }

    // ================= FORM =================

    private Inventory buildInventory() {

        InventoryManager.ProductChoice choice = selectedProduct();

        if (choice == null) {
            throw new IllegalArgumentException("Select a product first.");
        }

        int quantity = parseNonNegative(quantityField, "quantity on hand");
        int reorder = parseNonNegative(reorderField, "reorder level");

        return new Inventory(
                choice.getProductId(),
                choice.getProductName(),
                quantity,
                reorder,
                supplierField.getText().trim()
        );
    }

    private InventoryManager.ProductChoice selectedProduct() {

        Object selected = productCombo.getSelectedItem();

        return selected instanceof InventoryManager.ProductChoice
                ? (InventoryManager.ProductChoice) selected
                : null;
    }

    private int parsePositiveAmount() {

        String text = stockAmountField.getText().trim();

        try {
            int amount = Integer.parseInt(text);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a whole-number stock amount greater than zero.");
        }
    }

    private int parseNonNegative(JTextField field, String name) {

        String text = field.getText().trim();

        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a whole-number " + name + " (0 or more).");
        }
    }

    private void loadSelectedRow(int row) {

        String productId = cellText(row, 0);

        selectProduct(productId);

        quantityField.setText(cellText(row, 2));
        reorderField.setText(cellText(row, 3));
        supplierField.setText(cellText(row, 4));
        stockAmountField.setText("");
    }

    private void selectProduct(String productId) {

        for (int i = 0; i < productCombo.getItemCount(); i++) {
            InventoryManager.ProductChoice choice =
                    productCombo.getItemAt(i);
            if (choice.getProductId().equals(productId)) {
                productCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String cellText(int modelRow, int column) {
        Object value = tableModel.getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Stock & Inventory",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null ? "An unexpected error occurred." : message,
                "Stock & Inventory",
                JOptionPane.ERROR_MESSAGE);
    }
}
