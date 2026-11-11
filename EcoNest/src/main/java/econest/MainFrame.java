package econest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Supplier;

public class MainFrame extends JFrame {

    private JScrollPane moduleScroll;

    private record Module(
            String number,
            String title,
            String description,
            boolean built,
            Supplier<JFrame> frameFactory) {
    }

    private static final List<Module> MODULES = List.of(
            new Module("01", "Product Catalogue",
                    "Add, update and remove plants, planters and garden kits.",
                    true, ProductCatalogueFrame::new),
            new Module("06", "Delivery Assignment",
                    "Assign orders to available agents and track each delivery.",
                    true, DeliveryFrame::new),
            new Module("03", "Stock / Inventory",
                    "Quantities on hand, reorder levels and supplier records.",
                    true, InventoryFrame::new),
            new Module("04", "Delivery Agents",
                    "Agent profiles, availability and vehicle details.",
                    false, null),
            new Module("07", "Monthly Reports",
                    "Low-stock alerts and revenue summaries.",
                    true, ReportFrame::new),
            new Module("08", "Client Notifications",
                    "Email clients when their order is dispatched.",
                    false, null),
            new Module("02", "Client Management",
                    "Household and office clients with contact details.",
                    false, null),
            new Module("05", "Client Orders",
                    "Create orders, add products and calculate totals.",
                    false, null),
            new Module("09", "Agent Notifications",
                    "Email agents when a delivery is assigned.",
                    false, null)
    );

    public MainFrame() {

        setTitle("EcoNest - Home & Garden Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);
        setContentPane(createContent());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                Timer timer = new Timer(120, e -> {
                    scrollToTop();
                    ((Timer) e.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            }
        });

        setVisible(true);
    }

    private void scrollToTop() {

        if (moduleScroll != null) {
            moduleScroll.getViewport().setViewPosition(new Point(0, 0));
            moduleScroll.getVerticalScrollBar().setValue(0);
        }
    }

    private JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        moduleScroll = createModulePanel();

        root.add(createTopPanel(), BorderLayout.NORTH);
        root.add(moduleScroll, BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createTopPanel() {

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);

        top.add(new GreenHeader(
                "EcoNest",
                "Home & Garden Management - grow every part of your business."
        ), BorderLayout.NORTH);

        StatCard liveCard = new StatCard("Modules live", UITheme.MOSS);
        liveCard.setValue(4);
        liveCard.setHint("Catalogue, inventory, delivery & reports");

        StatCard plannedCard = new StatCard("In development", UITheme.WARNING);
        plannedCard.setValue(5);
        plannedCard.setHint("Built by the Solace team");

        StatCard totalCard = new StatCard("Project modules", UITheme.FOREST);
        totalCard.setValue(9);
        totalCard.setHint("One connected platform");

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(liveCard);
        stats.add(plannedCard);
        stats.add(totalCard);

        top.add(stats, BorderLayout.CENTER);

        return top;
    }

    private JScrollPane createModulePanel() {

        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        for (Module module : MODULES) {
            grid.add(createModuleCard(module));
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private RoundedPanel createModuleCard(Module module) {

        RoundedPanel card = new RoundedPanel(UITheme.CARD, 18);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setBorderColor(module.built() ? UITheme.SAGE : UITheme.BORDER);
        card.setPreferredSize(new Dimension(480, 188));

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);
        topRow.add(UITheme.badge(
                module.number(),
                module.built() ? UITheme.MINT : UITheme.PALE,
                module.built() ? UITheme.FOREST : UITheme.MUTED),
                BorderLayout.WEST);
        topRow.add(UITheme.badge(
                module.built() ? "LIVE" : "PLANNED",
                module.built() ? UITheme.MINT : UITheme.PALE,
                module.built() ? UITheme.FOREST : UITheme.MUTED),
                BorderLayout.EAST);

        JLabel title = new JLabel(module.title());
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(UITheme.DEEP);

        JTextArea description = new JTextArea(module.description());
        description.setFont(UITheme.BODY);
        description.setForeground(UITheme.MUTED);
        description.setOpaque(false);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFocusable(false);
        description.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel text = new JPanel(new BorderLayout());
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(description, BorderLayout.CENTER);

        JButton openButton = module.built()
                ? UITheme.primaryButton("Open module")
                : UITheme.ghostButton("Coming soon");

        openButton.setPreferredSize(new Dimension(0, 38));
        openButton.setFocusable(module.built());

        if (module.built()) {
            openButton.addActionListener(e -> {
                JFrame frame = module.frameFactory().get();
                frame.setVisible(true);
            });
        }

        card.add(topRow, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        card.add(openButton, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel left = UITheme.hint("Solace Group  -  OOP Project 2027");
        JLabel right = UITheme.hint("Designed with a greener footprint");

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }
}
