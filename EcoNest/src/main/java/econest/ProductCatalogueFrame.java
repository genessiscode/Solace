package econest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

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
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    private StatCard totalCard;
    private StatCard plantCard;
    private StatCard planterCard;
    private StatCard kitCard;

    private JLabel catalogueHint;
    private JLabel statusLabel;

    private ProductManager productManager;

    public ProductCatalogueFrame() {

        productManager = new ProductManager();

        setTitle("EcoNest - Product Catalogue");
        setSize(1320, 800);
        setMinimumSize(new Dimension(1080, 660));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(createContent());
        setVisible(true);

        updateTable();
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        root.add(createTopPanel(), BorderLayout.NORTH);
        root.add(createBodyPanel(), BorderLayout.CENTER);

        return root;
    }

    private JPanel createTopPanel() {

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);

        top.add(new GreenHeader(
                "Product Catalogue",
                "Curate your greenery - plants, planters and garden kits in one place."
        ), BorderLayout.NORTH);

        totalCard = new StatCard("Total products", UITheme.LEAF);
        plantCard = new StatCard("Plants", UITheme.MOSS);
        planterCard = new StatCard("Planters", UITheme.FOREST);
        kitCard = new StatCard("Garden kits", UITheme.SAGE);

        totalCard.setHint("Everything in stock");
        plantCard.setHint("Living greenery");
        planterCard.setHint("Pots & containers");
        kitCard.setHint("Starter projects");

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(totalCard);
        stats.add(plantCard);
        stats.add(planterCard);
        stats.add(kitCard);

        top.add(stats, BorderLayout.CENTER);

        return top;
    }

    private JPanel createBodyPanel() {

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(createFormCard(), BorderLayout.WEST);
        body.add(createTableCard(), BorderLayout.CENTER);
        return body;
    }

    private RoundedPanel createFormCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(380, 0));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        heading.add(UITheme.sectionTitle("Product details"));
        heading.add(UITheme.hint("Fill in the fields for the selected category."));

        JPanel form = new JPanel(new GridLayout(6, 2, 12, 10));
        form.setOpaque(false);

        productIdField = new JTextField();
        UITheme.styleField(productIdField);

        nameField = new JTextField();
        UITheme.styleField(nameField);

        categoryCombo = new JComboBox<>(ProductCategory.values());
        UITheme.styleCombo(categoryCombo);

        priceField = new JTextField();
        UITheme.styleField(priceField);

        ecoRatingField = new JTextField();
        UITheme.styleField(ecoRatingField);

        descriptionField = new JTextField();
        UITheme.styleField(descriptionField);

        sunlightCombo = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
        UITheme.styleCombo(sunlightCombo);

        petSafeCheckBox = new JCheckBox("Safe for pets");
        petSafeCheckBox.setFont(UITheme.BODY);
        petSafeCheckBox.setForeground(UITheme.TEXT);
        petSafeCheckBox.setOpaque(false);
        petSafeCheckBox.setFocusPainted(false);

        materialField = new JTextField();
        UITheme.styleField(materialField);

        diameterField = new JTextField();
        UITheme.styleField(diameterField);

        difficultyField = new JTextField();
        UITheme.styleField(difficultyField);

        form.add(fieldGroup("Product ID", productIdField));
        form.add(fieldGroup("Product name", nameField));
        form.add(fieldGroup("Category", categoryCombo));
        form.add(fieldGroup("Price (Rs.)", priceField));
        form.add(fieldGroup("Eco rating (1-5)", ecoRatingField));
        form.add(fieldGroup("Description", descriptionField));
        form.add(fieldGroup("Sunlight", sunlightCombo));
        form.add(fieldGroup("Pet safe", petSafeCheckBox));
        form.add(fieldGroup("Material", materialField));
        form.add(fieldGroup("Diameter (cm)", diameterField));
        form.add(fieldGroup("Difficulty", difficultyField));

        JPanel tip = new JPanel(new BorderLayout());
        tip.setOpaque(false);
        tip.setBorder(BorderFactory.createEmptyBorder(20, 2, 0, 2));
        tip.add(UITheme.hint("<html>Subtype fields enable<br>automatically.</html>"),
                BorderLayout.NORTH);

        form.add(tip);

        JButton addButton = UITheme.primaryButton("Add product");
        JButton updateButton = UITheme.secondaryButton("Update");
        JButton removeButton = UITheme.dangerButton("Remove");
        JButton clearButton = UITheme.ghostButton("Clear form");

        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(removeButton);
        actions.add(clearButton);

        card.add(heading, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        categoryCombo.addActionListener(e -> updateSubtypeControls());
        updateSubtypeControls();

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        removeButton.addActionListener(e -> removeProduct());
        clearButton.addActionListener(e -> clearFields());

        return card;
    }

    private RoundedPanel createTableCard() {

        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel heading = new JPanel(new BorderLayout(12, 0));
        heading.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 3));
        titles.setOpaque(false);
        titles.add(UITheme.sectionTitle("Catalogue board"));
        catalogueHint = UITheme.hint("0 products in catalogue");
        titles.add(catalogueHint);
        heading.add(titles, BorderLayout.WEST);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        tools.setOpaque(false);
        tools.add(UITheme.hint("Search"));
        searchField = new JTextField(16);
        UITheme.styleField(searchField);
        searchField.setToolTipText("Filter by name, category or description");
        tools.add(searchField);

        JButton refreshButton = UITheme.secondaryButton("Refresh");
        tools.add(refreshButton);
        heading.add(tools, BorderLayout.EAST);

        String[] columns = {
                "Product ID",
                "Name",
                "Category",
                "Price",
                "Eco",
                "Description",
                "Detail"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        UITheme.styleTable(productTable);

        int[] widths = {100, 160, 90, 80, 50, 235, 145};
        for (int i = 0; i < widths.length; i++) {
            productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);

        JScrollPane scrollPane = UITheme.scroll(productTable);

        statusLabel = UITheme.hint("Ready");
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));
        footer.add(statusLabel, BorderLayout.WEST);

        card.add(heading, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> updateTable());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFromSelection();
            }
        });

        return card;
    }

    private static JPanel fieldGroup(String label, java.awt.Component input) {

        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.add(UITheme.fieldLabel(label), BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
    }

    private void applyFilter() {

        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(text)));
        }

        updateCatalogueHint();
    }

    private void fillFromSelection() {

        int row = productTable.getSelectedRow();

        if (row < 0) {
            return;
        }

        int modelRow = productTable.convertRowIndexToModel(row);
        String productId = String.valueOf(tableModel.getValueAt(modelRow, 0));

        try {

            Product product = productManager.findProduct(productId);

            if (product != null) {
                fillForm(product);
            }

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void addProduct() {

        try {

            Product product = buildProduct();
            Product existing = productManager.findProduct(product.getProductId());

            if (existing != null) {
                showError("Product ID already exists.");
                return;
            }

            productManager.addProduct(product);
            JOptionPane.showMessageDialog(
                    this,
                    "Product added to the catalogue.",
                    "Product Catalogue",
                    JOptionPane.INFORMATION_MESSAGE);

            updateTable();
            clearFields();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateProduct() {

        try {

            Product product = buildProduct();
            productManager.updateProduct(product);
            JOptionPane.showMessageDialog(
                    this,
                    "Product updated successfully.",
                    "Product Catalogue",
                    JOptionPane.INFORMATION_MESSAGE);

            updateTable();
            clearFields();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeProduct() {

        String productId = productIdField.getText().trim();

        if (productId.isEmpty()) {
            showError("Please select or enter a Product ID.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove this product?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean removed = productManager.removeProduct(productId);

            if (removed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Product removed successfully.",
                        "Product Catalogue",
                        JOptionPane.INFORMATION_MESSAGE);
                updateTable();
                clearFields();
            } else {
                showError("Cannot remove: product still has stock"
                        + " or order history.");
            }

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private Product buildProduct() {

        String productId = productIdField.getText().trim();
        String name = nameField.getText().trim();

        if (productId.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter Product ID and Product Name.");
        }

        double price;

        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Please enter a valid price.");
        }

        int ecoRating;

        try {
            ecoRating = Integer.parseInt(ecoRatingField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Please enter a valid eco rating (1-5).");
        }

        String description = descriptionField.getText().trim();
        ProductCategory category =
                (ProductCategory) categoryCombo.getSelectedItem();

        switch (category) {

            case PLANT:
                return new Plant(
                        productId, name, price, ecoRating, description,
                        (String) sunlightCombo.getSelectedItem(),
                        petSafeCheckBox.isSelected());

            case PLANTER:
                int diameterCm;

                try {
                    diameterCm = Integer.parseInt(
                            diameterField.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Please enter a valid diameter in cm.");
                }

                return new Planter(
                        productId, name, price, ecoRating, description,
                        materialField.getText().trim(), diameterCm);

            case KIT:
            default:
                return new GardenKit(
                        productId, name, price, ecoRating, description,
                        difficultyField.getText().trim());
        }
    }

    private void fillForm(Product product) {

        productIdField.setText(product.getProductId());
        nameField.setText(product.getName());
        categoryCombo.setSelectedItem(product.getCategory());
        priceField.setText(String.format(
                Locale.ROOT, "%.2f", product.getPrice()));
        ecoRatingField.setText(String.valueOf(product.getEcoRating()));
        descriptionField.setText(product.getDescription());

        if (product instanceof Plant) {
            Plant plant = (Plant) product;
            sunlightCombo.setSelectedItem(plant.getSunlight());
            petSafeCheckBox.setSelected(plant.isPetSafe());
        } else if (product instanceof Planter) {
            Planter planter = (Planter) product;
            materialField.setText(planter.getMaterial());
            diameterField.setText(String.valueOf(planter.getDiameterCm()));
        } else if (product instanceof GardenKit) {
            difficultyField.setText(((GardenKit) product).getDifficulty());
        }

        updateSubtypeControls();
    }

    private void updateSubtypeControls() {

        ProductCategory category =
                (ProductCategory) categoryCombo.getSelectedItem();

        boolean isPlant = category == ProductCategory.PLANT;
        boolean isPlanter = category == ProductCategory.PLANTER;
        boolean isKit = category == ProductCategory.KIT;

        sunlightCombo.setEnabled(isPlant);
        petSafeCheckBox.setEnabled(isPlant);

        materialField.setEnabled(isPlanter);
        diameterField.setEnabled(isPlanter);

        difficultyField.setEnabled(isKit);
    }

    private static String buildDetail(Product product) {

        if (product instanceof Plant) {
            Plant plant = (Plant) product;
            return plant.getSunlight()
                    + (plant.isPetSafe() ? " - pet safe" : "");
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

            ArrayList<Product> list = productManager.getProductList();

            int plants = 0;
            int planters = 0;
            int kits = 0;

            for (Product product : list) {

                tableModel.addRow(new Object[]{
                        product.getProductId(),
                        product.getName(),
                        product.getCategory().name(),
                        String.format(Locale.ROOT, "%.2f", product.getPrice()),
                        product.getEcoRating(),
                        product.getDescription(),
                        buildDetail(product)
                });

                switch (product.getCategory()) {
                    case PLANT:
                        plants++;
                        break;
                    case PLANTER:
                        planters++;
                        break;
                    case KIT:
                    default:
                        kits++;
                        break;
                }
            }

            totalCard.setValue(list.size());
            plantCard.setValue(plants);
            planterCard.setValue(planters);
            kitCard.setValue(kits);

            statusLabel.setForeground(UITheme.MUTED);
            statusLabel.setText("Ready - database connected");
            updateCatalogueHint();

        } catch (IllegalStateException ex) {

            statusLabel.setForeground(UITheme.DANGER);
            statusLabel.setText("Database unavailable - check config.properties");
            showError(ex.getMessage());
        }
    }

    private void updateCatalogueHint() {

        int total = tableModel.getRowCount();
        int shown = productTable.getRowCount();

        if (shown == total) {
            catalogueHint.setText(total + " products in catalogue");
        } else {
            catalogueHint.setText(shown + " of " + total + " products shown");
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

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Product Catalogue",
                JOptionPane.ERROR_MESSAGE);
    }
}
