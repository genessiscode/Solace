package econest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class InventoryFrame extends JFrame {

    private JTextField productIdField;
    private JTextField productNameField;
    private JTextField quantityField;
    private JTextField reorderLevelField;
    private JTextField supplierField;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    private InventoryManager inventoryManager;

    public InventoryFrame() {

        inventoryManager = new InventoryManager();

        setTitle("EcoNest - Inventory Management");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
                new GridLayout(5, 2, 10, 10)
        );

        inputPanel.add(new JLabel("Product ID:"));

        productIdField = new JTextField();
        inputPanel.add(productIdField);

        inputPanel.add(new JLabel("Product Name:"));

        productNameField = new JTextField();
        inputPanel.add(productNameField);

        inputPanel.add(new JLabel("Quantity:"));

        quantityField = new JTextField();
        inputPanel.add(quantityField);

        inputPanel.add(new JLabel("Reorder Level:"));

        reorderLevelField = new JTextField();
        inputPanel.add(reorderLevelField);

        inputPanel.add(new JLabel("Supplier Name:"));

        supplierField = new JTextField();
        inputPanel.add(supplierField);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // ================= TABLE =================

        String[] columns = {
                "Product ID",
                "Product Name",
                "Quantity",
                "Reorder Level",
                "Status"
        };

        tableModel = new DefaultTableModel(
                columns,
                0
        );

        inventoryTable = new JTable(tableModel);

        JScrollPane scrollPane =
                new JScrollPane(inventoryTable);

        mainPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // ================= BUTTON PANEL =================

        JPanel buttonPanel = new JPanel(
                new GridLayout(2, 4, 10, 10)
        );

        JButton addProductButton =
                new JButton("Add Product");

        JButton updateProductButton =
                new JButton("Update Product");

        JButton removeProductButton =
                new JButton("Remove Product");

        JButton addStockButton =
                new JButton("Stock-In");

        JButton removeStockButton =
                new JButton("Remove Stock");

        JButton checkStockButton =
                new JButton("Check Stock");

        JButton lowStockButton =
                new JButton("View Low Stock");

        JButton clearButton =
                new JButton("Clear");

        buttonPanel.add(addProductButton);
        buttonPanel.add(updateProductButton);
        buttonPanel.add(removeProductButton);
        buttonPanel.add(addStockButton);

        buttonPanel.add(removeStockButton);
        buttonPanel.add(checkStockButton);
        buttonPanel.add(lowStockButton);
        buttonPanel.add(clearButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        // ================= TABLE ROW CLICK =================

        inventoryTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int row =
                                inventoryTable.getSelectedRow();

                        if (row >= 0) {

                            productIdField.setText(
                                    tableModel.getValueAt(
                                            row, 0
                                    ).toString()
                            );

                            productNameField.setText(
                                    tableModel.getValueAt(
                                            row, 1
                                    ).toString()
                            );

                            quantityField.setText(
                                    tableModel.getValueAt(
                                            row, 2
                                    ).toString()
                            );

                            reorderLevelField.setText(
                                    tableModel.getValueAt(
                                            row, 3
                                    ).toString()
                            );
                        }
                    }
                });

        // ================= ADD PRODUCT =================

        addProductButton.addActionListener(e -> {

            String productId =
                    productIdField.getText().trim();

            String productName =
                    productNameField.getText().trim();

            String quantityText =
                    quantityField.getText().trim();

            String reorderText =
                    reorderLevelField.getText().trim();

            if (productId.isEmpty()
                    || productName.isEmpty()
                    || quantityText.isEmpty()
                    || reorderText.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please fill all required fields."
                );

                return;
            }

            try {

                int quantity =
                        Integer.parseInt(quantityText);

                int reorderLevel =
                        Integer.parseInt(reorderText);

                if (quantity < 0
                        || reorderLevel < 0) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Quantity and reorder level cannot be negative."
                    );

                    return;
                }

                Inventory existing =
                        inventoryManager.findProduct(
                                productId
                        );

                if (existing != null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Product ID already exists."
                    );

                    return;
                }

                Inventory item =
                        new Inventory(
                                productId,
                                productName,
                                quantity,
                                reorderLevel
                        );

                inventoryManager.addInventory(item);

                JOptionPane.showMessageDialog(
                        this,
                        "Product Added Successfully!"
                );

                updateTable();
                clearFields();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter valid numbers."
                );
            }
        });

        // ================= UPDATE PRODUCT =================

        updateProductButton.addActionListener(e -> {

            String productId =
                    productIdField.getText().trim();

            String productName =
                    productNameField.getText().trim();

            if (productId.isEmpty()
                    || productName.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter Product ID and Product Name."
                );

                return;
            }

            try {

                int quantity =
                        Integer.parseInt(
                                quantityField.getText().trim()
                        );

                int reorderLevel =
                        Integer.parseInt(
                                reorderLevelField.getText().trim()
                        );

                Inventory updatedItem =
                        new Inventory(
                                productId,
                                productName,
                                quantity,
                                reorderLevel
                        );

                inventoryManager.updateInventory(
                        productId,
                        updatedItem
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Product Updated Successfully!"
                );

                updateTable();
                clearFields();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter valid numbers."
                );
            }
        });

        // ================= REMOVE PRODUCT =================

        removeProductButton.addActionListener(e -> {

            String productId =
                    productIdField.getText().trim();

            if (productId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select or enter a Product ID."
                );

                return;
            }

            int confirm =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to remove this product?",
                            "Confirm Remove",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirm
                    == JOptionPane.YES_OPTION) {

                inventoryManager.removeInventory(
                        productId
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Product Removed Successfully!"
                );

                updateTable();
                clearFields();
            }
        });

        // ================= STOCK-IN =================

        addStockButton.addActionListener(e -> {

            String productId =
                    productIdField.getText().trim();

            String supplier =
                    supplierField.getText().trim();

            if (productId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select or enter a Product ID."
                );

                return;
            }

            if (supplier.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter Supplier Name."
                );

                return;
            }

            try {

                int amount =
                        Integer.parseInt(
                                quantityField.getText().trim()
                        );

                if (amount <= 0) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a valid stock quantity."
                    );

                    return;
                }

                Inventory item =
                        inventoryManager.findProduct(
                                productId
                        );

                if (item == null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Product not found."
                    );

                    return;
                }

                int oldStock =
                        item.getQuantity();

                // UPDATED: supplier name is now sent
                inventoryManager.addStock(
                        productId,
                        amount,
                        supplier
                );

                int newStock =
                        oldStock + amount;

                JOptionPane.showMessageDialog(
                        this,
                        "Stock-In Successful!\n\n"
                                + "Product: "
                                + item.getProductName()
                                + "\nSupplier: "
                                + supplier
                                + "\nAdded Quantity: "
                                + amount
                                + "\nPrevious Stock: "
                                + oldStock
                                + "\nCurrent Stock: "
                                + newStock
                );

                updateTable();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid quantity."
                );
            }
        });

        // ================= REMOVE STOCK =================

        removeStockButton.addActionListener(e -> {

            String productId =
                    productIdField.getText().trim();

            if (productId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select or enter a Product ID."
                );

                return;
            }

            try {

                int amount =
                        Integer.parseInt(
                                quantityField.getText().trim()
                        );

                if (amount <= 0) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a valid quantity."
                    );

                    return;
                }

                Inventory item =
                        inventoryManager.findProduct(
                                productId
                        );

                if (item == null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Product not found."
                    );

                    return;
                }

                if (amount > item.getQuantity()) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Not enough stock available."
                    );

                    return;
                }

                inventoryManager.removeStock(
                        productId,
                        amount
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Stock Removed Successfully!"
                );

                updateTable();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid quantity."
                );
            }
        });

        // ================= CHECK STOCK =================

        checkStockButton.addActionListener(e -> {

            String productId =
                    productIdField.getText().trim();

            if (productId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter or select a Product ID."
                );

                return;
            }

            Inventory item =
                    inventoryManager.findProduct(
                            productId
                    );

            if (item == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Product not found."
                );

                return;
            }

            if (item.isLowStock()) {

                JOptionPane.showMessageDialog(
                        this,
                        "LOW STOCK!\n\n"
                                + "Product: "
                                + item.getProductName()
                                + "\nCurrent Stock: "
                                + item.getQuantity()
                                + "\nReorder Level: "
                                + item.getReorderLevel()
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Stock is OK.\n\n"
                                + "Product: "
                                + item.getProductName()
                                + "\nCurrent Stock: "
                                + item.getQuantity()
                );
            }
        });

        // ================= VIEW LOW STOCK =================

        lowStockButton.addActionListener(e -> {

            ArrayList<Inventory> list =
                    inventoryManager.getInventoryList();

            StringBuilder message =
                    new StringBuilder();

            boolean found = false;

            for (Inventory item : list) {

                if (item.isLowStock()) {

                    found = true;

                    message.append(
                            "Product ID: "
                    ).append(
                            item.getProductId()
                    ).append(
                            "\nProduct: "
                    ).append(
                            item.getProductName()
                    ).append(
                            "\nCurrent Stock: "
                    ).append(
                            item.getQuantity()
                    ).append(
                            "\nReorder Level: "
                    ).append(
                            item.getReorderLevel()
                    ).append(
                            "\n\n"
                    );
                }
            }

            if (found) {

                JOptionPane.showMessageDialog(
                        this,
                        message.toString(),
                        "Low Stock Products",
                        JOptionPane.WARNING_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No low stock products."
                );
            }
        });

        // ================= CLEAR =================

        clearButton.addActionListener(e -> {

            clearFields();

        });
    }

    // ================= UPDATE TABLE =================

    private void updateTable() {

        tableModel.setRowCount(0);

        ArrayList<Inventory> list =
                inventoryManager.getInventoryList();

        for (Inventory item : list) {

            String status;

            if (item.isLowStock()) {

                status = "LOW STOCK";

            } else {

                status = "OK";
            }

            tableModel.addRow(
                    new Object[]{
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getReorderLevel(),
                            status
                    }
            );
        }
    }

    // ================= CLEAR FIELDS =================

    private void clearFields() {

        productIdField.setText("");
        productNameField.setText("");
        quantityField.setText("");
        reorderLevelField.setText("");
        supplierField.setText("");

        inventoryTable.clearSelection();
    }
}