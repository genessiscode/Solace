package econest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Supplier;

/**
 * The EcoNest dashboard. Lists every module as a card and opens each one in
 * its own window, so modules stay runnable on their own.
 */
public class MainFrame extends JFrame {

    private JLabel productsValueLabel;

    private JLabel lowStockValueLabel;

    private final ProductManager productManager = new ProductManager();

    private final InventoryManager inventoryManager = new InventoryManager();

    /**
     * One dashboard module.
     *
     * <p>{@code frameFactory} builds the module's window, or is {@code null}
     * when no frame class exists yet. Holding the factory here means the
     * dashboard never has to match on the card's title text, and a module
     * with no factory structurally cannot open the wrong window.</p>
     */
    private record Module(
            String title,
            String description,
            Supplier<JFrame> frameFactory) {

        boolean isBuilt() {
            return frameFactory != null;
        }
    }

    /**
     * Every module from the project brief. Only modules 1-5 have a frame
     * class; 6-9 are listed so the dashboard shows the full project scope,
     * with disabled buttons.
     */
    private static final List<Module> MODULES = List.of(
            new Module("1. Product Catalogue",
                    "Add, update and remove plants, planters and kits",
                    ProductCatalogueFrame::new),
            new Module("2. Clients",
                    "Household and office clients with contact details",
                    ClientFrame::new),
            new Module("3. Stock / Inventory",
                    "Quantity on hand, reorder levels and stock-in",
                    InventoryFrame::new),
            new Module("4. Delivery Agents",
                    "Delivery personnel and their vehicle details",
                    AgentFrame::new),
            new Module("5. Client Orders",
                    "Create orders, assign products, calculate totals",
                    OrderFrame::new),
            new Module("6. Delivery Assignment",
                    "Schedule and track deliveries per order",
                    null),
            new Module("7. Monthly Reports",
                    "Low-stock alerts and revenue summaries",
                    null),
            new Module("8. Client Notifications",
                    "Email the client when an order is dispatched",
                    null),
            new Module("9. Agent Notifications",
                    "Email the agent when a delivery is assigned",
                    null)
    );

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

        for (Module module : MODULES) {

            grid.add(createModuleCard(
                    module.title(),
                    module.description(),
                    module.isBuilt()));
        }

        return grid;
    }

    /**
     * One module card: a title, a description, a status line and an Open
     * button.
     *
     * @param enabled whether a frame exists for this module yet
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

        JLabel statusLabel = new JLabel();

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        if (enabled) {

            statusLabel.setText("Ready");
            openButton.addActionListener(e -> openModule(title));

        } else {

            // No frame class exists yet, so the button is disabled and the
            // card says so rather than opening a "coming soon" dialog.
            openButton.setEnabled(false);
            statusLabel.setText("Not built yet");
            statusLabel.setForeground(Color.GRAY);
            openButton.setToolTipText("Not built yet");
            card.setToolTipText("Not built yet");
        }

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));

        southPanel.add(statusLabel, BorderLayout.NORTH);
        southPanel.add(openButton, BorderLayout.SOUTH);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descriptionArea, BorderLayout.CENTER);
        card.add(southPanel, BorderLayout.SOUTH);

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
     *
     * <p>Only reachable for modules whose Open button is enabled, so every
     * branch here corresponds to a frame class that exists.</p>
     */
    private void openModule(String title) {

        JFrame moduleWindow;

        // Find the module whose factory produces this title's window.
        Module matched = null;
        for (Module m : MODULES) {
            if (m.title().equals(title)) { matched = m; break; }
        }
        if (matched == null || !matched.isBuilt()) {
            JOptionPane.showMessageDialog(this, "No window available for: " + title);
            return;
        }
        moduleWindow = matched.frameFactory().get();

        // Defensive: never open a module whose factory is null (should not happen).
        if (moduleWindow == null) {
            JOptionPane.showMessageDialog(
                    this, "Window factory returned null for: " + title);
            return;
        }

        moduleWindow.setVisible(true);

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
