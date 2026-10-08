package econest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The EcoNest dashboard. Lists every module as a card and opens each one in
 * its own window, so modules stay runnable on their own.
 */
public class MainFrame extends JFrame {

    private JLabel productsValueLabel;

    private JLabel lowStockValueLabel;

    private final ProductManager productManager = new ProductManager();

    private final InventoryManager inventoryManager = new InventoryManager();

    /** One row per module: title, description, and whether it is built yet. */
    private static final String[][] MODULES = {
            {"1. Product Catalogue", "Add, update and remove plants, planters and kits", "no"},
            {"2. Clients", "Household and office clients with contact details", "no"},
            {"3. Stock / Inventory", "Quantity on hand, reorder levels and stock-in", "yes"},
            {"4. Delivery Agents", "Delivery personnel and their vehicle details", "no"},
            {"5. Client Orders", "Create orders, assign products, calculate totals", "no"},
            {"6. Delivery Assignment", "Schedule and track deliveries per order", "no"},
            {"7. Monthly Reports", "Low-stock alerts and revenue summaries", "no"},
            {"8. Client Notifications", "Email the client when an order is dispatched", "no"},
            {"9. Agent Notifications", "Email the agent when a delivery is assigned", "no"}
    };

    public MainFrame() {

        setTitle("EcoNest - Home & Garden Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        createGUI();

        refreshSummaries();

        setVisible(true);
    }

    // ================= CREATE GUI =================

    private void createGUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        mainPanel.add(createModulePanel(), BorderLayout.CENTER);

        mainPanel.add(createSummaryPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    /** Title and subtitle across the top. */
    private JPanel createHeaderPanel() {

        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel("EcoNest");

        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Home & Garden Management - select a module");

        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    /** A three-column grid of module cards. */
    private JPanel createModulePanel() {

        JPanel grid = new JPanel(new GridLayout(3, 3, 12, 12));

        for (String[] module : MODULES) {

            grid.add(createModuleCard(
                    module[0],
                    module[1],
                    "yes".equals(module[2])));
        }

        return grid;
    }

    /**
     * One module card: a title, a description and an Open button.
     *
     * @param enabled whether the module is built yet
     */
    private JPanel createModuleCard(
            String title,
            String description,
            boolean enabled) {

        JPanel card = new JPanel(new BorderLayout(5, 5));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JTextArea descriptionArea = new JTextArea(description);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JButton openButton = new JButton("Open");

        openButton.setEnabled(enabled);

        if (!enabled) {
            openButton.setToolTipText("Coming in a later build");
            card.setToolTipText("Coming in a later build");
        } else {
            openButton.addActionListener(e -> openModule(title));
        }

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descriptionArea, BorderLayout.CENTER);
        card.add(openButton, BorderLayout.SOUTH);

        return card;
    }

    /** Two live tiles read from the database. */
    private JPanel createSummaryPanel() {

        JPanel summary = new JPanel(new GridLayout(1, 2, 12, 0));

        productsValueLabel = new JLabel("-", SwingConstants.CENTER);

        productsValueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        lowStockValueLabel = new JLabel("-", SwingConstants.CENTER);

        lowStockValueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        summary.add(createSummaryTile("Products in catalogue", productsValueLabel));
        summary.add(createSummaryTile("Low stock items", lowStockValueLabel));

        return summary;
    }

    private JPanel createSummaryTile(String caption, JLabel valueLabel) {

        JPanel tile = new JPanel(new BorderLayout());

        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel captionLabel = new JLabel(caption, SwingConstants.CENTER);

        captionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        tile.add(captionLabel, BorderLayout.NORTH);
        tile.add(valueLabel, BorderLayout.CENTER);

        return tile;
    }

    // ================= ACTIONS =================

    /**
     * Opens a module window. Each module disposes rather than exits, so this
     * dashboard stays alive and refreshes its summaries when the module closes.
     */
    private void openModule(String title) {

        JFrame moduleWindow;

        if (title.startsWith("3.")) {

            moduleWindow = new InventoryFrame();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "This module is not built yet.");

            return;
        }

        moduleWindow.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                refreshSummaries();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                refreshSummaries();
            }
        });
    }

    /** Re-reads the two summary numbers, tolerating a database that is down. */
    private void refreshSummaries() {

        try {

            productsValueLabel.setText(
                    String.valueOf(productManager.countProducts()));

            lowStockValueLabel.setText(
                    String.valueOf(inventoryManager.countLowStockItems()));

        } catch (RuntimeException e) {

            productsValueLabel.setText("n/a");
            lowStockValueLabel.setText("n/a");
        }
    }
}
