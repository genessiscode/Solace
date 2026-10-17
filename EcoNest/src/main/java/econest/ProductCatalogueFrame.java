package econest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public class ProductCatalogueFrame extends JFrame {

    private JTextField productIdField;
    private JTextField nameField;
    private JComboBox<ProductCategory> categoryCombo;
    private JTextField priceField;
    private JTextField ecoRatingField;
    private JTextField descriptionField;

    private JComboBox<String> sunlightCombo;
    private JCheckBox petSafeCheckBox;
    private JTextField materialField;
    private JTextField diameterField;
    private JTextField difficultyField;

    private JTable productTable;
    private DefaultTableModel tableModel;

    private ProductManager productManager;

    public ProductCatalogueFrame() {

        productManager = new ProductManager();

        setTitle("EcoNest - Product Catalogue");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        createGUI();

        updateTable();

        setVisible(true);
    }

    private void createGUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10
                )
        );

        JPanel inputPanel = new JPanel(
                new GridLayout(11, 2, 10, 10)
        );

        inputPanel.add(new JLabel("Product ID:"));

        productIdField = new JTextField();
        inputPanel.add(productIdField);

        inputPanel.add(new JLabel("Product Name:"));

        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Category:"));

        categoryCombo = new JComboBox<>(ProductCategory.values());
        inputPanel.add(categoryCombo);

        inputPanel.add(new JLabel("Price (Rs.):"));

        priceField = new JTextField();
        inputPanel.add(priceField);

        inputPanel.add(new JLabel("Eco Rating (1-5):"));

        ecoRatingField = new JTextField();
        inputPanel.add(ecoRatingField);

        inputPanel.add(new JLabel("Description:"));

        descriptionField = new JTextField();
        inputPanel.add(descriptionField);

        inputPanel.add(new JLabel("Sunlight:"));

        sunlightCombo = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
        inputPanel.add(sunlightCombo);

        inputPanel.add(new JLabel("Pet Safe:"));

        petSafeCheckBox = new JCheckBox();
        inputPanel.add(petSafeCheckBox);

        inputPanel.add(new JLabel("Material:"));

        materialField = new JTextField();
        inputPanel.add(materialField);

        inputPanel.add(new JLabel("Diameter (cm):"));

        diameterField = new JTextField();
        inputPanel.add(diameterField);

        inputPanel.add(new JLabel("Difficulty:"));

        difficultyField = new JTextField();
        inputPanel.add(difficultyField);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        String[] columns = {
                "Product ID",
                "Name",
                "Category",
                "Price",
                "Eco Rating",
                "Description",
                "Detail"
        };

        tableModel = new DefaultTableModel(
                columns,
                0
        );

        productTable = new JTable(tableModel);

        JScrollPane scrollPane =
                new JScrollPane(productTable);

        mainPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 5, 10, 10)
        );

        JButton addProductButton =
                new JButton("Add Product");

        JButton updateProductButton =
                new JButton("Update Product");

        JButton removeProductButton =
                new JButton("Remove Product");

        JButton clearButton =
                new JButton("Clear");

        JButton refreshButton =
                new JButton("Refresh");

        buttonPanel.add(addProductButton);
        buttonPanel.add(updateProductButton);
        buttonPanel.add(removeProductButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        categoryCombo.addActionListener(e -> updateSubtypeControls());

        updateSubtypeControls();

        productTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int row =
                                productTable.getSelectedRow();

                        if (row >= 0) {

                            String productId =
                                    tableModel.getValueAt(
                                            row, 0
                                    ).toString();

                            try {

                                Product product =
                                        productManager.findProduct(
                                                productId
                                        );

                                if (product != null) {

                                    fillForm(product);
                                }

                            } catch (IllegalStateException ex) {

                                JOptionPane.showMessageDialog(
                                        this,
                                        ex.getMessage()
                                );
                            }
                        }
                    }
                });

        addProductButton.addActionListener(e -> {

            try {

                Product product = buildProduct();

                Product existing =
                        productManager.findProduct(
                                product.getProductId()
                        );

                if (existing != null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Product ID already exists."
                    );

                    return;
                }

                productManager.addProduct(product);

                JOptionPane.showMessageDialog(
                        this,
                        "Product Added Successfully!"
                );

                updateTable();
                clearFields();

            } catch (IllegalArgumentException | IllegalStateException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage()
                );
            }
        });

        updateProductButton.addActionListener(e -> {

            try {

                Product product = buildProduct();

                productManager.updateProduct(product);

                JOptionPane.showMessageDialog(
                        this,
                        "Product Updated Successfully!"
                );

                updateTable();
                clearFields();

            } catch (IllegalArgumentException | IllegalStateException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage()
                );
            }
        });

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

                try {

                    boolean removed =
                            productManager.removeProduct(
                                    productId
                            );

                    if (removed) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Product Removed Successfully!"
                        );

                        updateTable();
                        clearFields();

                    } else {

                        JOptionPane.showMessageDialog(
                                this,
                                "Cannot remove: product still has stock"
                                        + " or order history."
                        );
                    }

                } catch (IllegalStateException ex) {

                    JOptionPane.showMessageDialog(
                            this,
                            ex.getMessage()
                    );
                }
            }
        });

        clearButton.addActionListener(e -> {

            clearFields();

        });

        refreshButton.addActionListener(e -> {

            updateTable();

        });
    }

    private Product buildProduct() {

        String productId =
                productIdField.getText().trim();

        String name =
                nameField.getText().trim();

        if (productId.isEmpty() || name.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please enter Product ID and Product Name.");
        }

        double price;

        try {

            price = Double.parseDouble(
                    priceField.getText().trim()
            );

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "Please enter a valid price.");
        }

        int ecoRating;

        try {

            ecoRating = Integer.parseInt(
                    ecoRatingField.getText().trim()
            );

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "Please enter a valid eco rating (1-5).");
        }

        String description =
                descriptionField.getText().trim();

        ProductCategory category =
                (ProductCategory) categoryCombo.getSelectedItem();

        switch (category) {

            case PLANT:
                return new Plant(
                        productId, name, price, ecoRating, description,
                        (String) sunlightCombo.getSelectedItem(),
                        petSafeCheckBox.isSelected()
                );

            case PLANTER:
                int diameterCm;

                try {

                    diameterCm = Integer.parseInt(
                            diameterField.getText().trim()
                    );

                } catch (NumberFormatException ex) {

                    throw new IllegalArgumentException(
                            "Please enter a valid diameter in cm.");
                }

                return new Planter(
                        productId, name, price, ecoRating, description,
                        materialField.getText().trim(),
                        diameterCm
                );

            case KIT:
            default:
                return new GardenKit(
                        productId, name, price, ecoRating, description,
                        difficultyField.getText().trim()
                );
        }
    }

    private void fillForm(Product product) {

        productIdField.setText(product.getProductId());
        nameField.setText(product.getName());

        categoryCombo.setSelectedItem(product.getCategory());

        priceField.setText(String.format(
                Locale.ROOT,
                "%.2f",
                product.getPrice()
        ));

        ecoRatingField.setText(
                String.valueOf(product.getEcoRating())
        );

        descriptionField.setText(product.getDescription());

        if (product instanceof Plant) {

            Plant plant = (Plant) product;

            sunlightCombo.setSelectedItem(plant.getSunlight());
            petSafeCheckBox.setSelected(plant.isPetSafe());

        } else if (product instanceof Planter) {

            Planter planter = (Planter) product;

            materialField.setText(planter.getMaterial());
            diameterField.setText(
                    String.valueOf(planter.getDiameterCm())
            );

        } else if (product instanceof GardenKit) {

            difficultyField.setText(
                    ((GardenKit) product).getDifficulty()
            );
        }

        updateSubtypeControls();
    }

    private void updateSubtypeControls() {

        ProductCategory category =
                (ProductCategory) categoryCombo.getSelectedItem();

        boolean isPlant =
                category == ProductCategory.PLANT;

        boolean isPlanter =
                category == ProductCategory.PLANTER;

        boolean isKit =
                category == ProductCategory.KIT;

        sunlightCombo.setEnabled(isPlant);
        petSafeCheckBox.setEnabled(isPlant);

        materialField.setEnabled(isPlanter);
        diameterField.setEnabled(isPlanter);

        difficultyField.setEnabled(isKit);
    }

    private static String buildDetail(Product product) {

        if (product instanceof Plant) {

            return ((Plant) product).getSunlight();

        } else if (product instanceof Planter) {

            Planter planter = (Planter) product;

            return planter.getMaterial()
                    + " (" + planter.getDiameterCm() + " cm)";

        } else if (product instanceof GardenKit) {

            return ((GardenKit) product).getDifficulty();
        }

        return "";
    }

    private void updateTable() {

        tableModel.setRowCount(0);

        try {

            ArrayList<Product> list =
                    productManager.getProductList();

            for (Product product : list) {

                tableModel.addRow(
                        new Object[]{
                                product.getProductId(),
                                product.getName(),
                                product.getCategory().name(),
                                String.format(
                                        Locale.ROOT,
                                        "%.2f",
                                        product.getPrice()
                                ),
                                product.getEcoRating(),
                                product.getDescription(),
                                buildDetail(product)
                        }
                );
            }

        } catch (IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    private void clearFields() {

        productIdField.setText("");
        nameField.setText("");
        categoryCombo.setSelectedIndex(0);
        priceField.setText("");
        ecoRatingField.setText("");
        descriptionField.setText("");

        sunlightCombo.setSelectedIndex(0);
        petSafeCheckBox.setSelected(false);
        materialField.setText("");
        diameterField.setText("");
        difficultyField.setText("");

        productTable.clearSelection();
    }
}
