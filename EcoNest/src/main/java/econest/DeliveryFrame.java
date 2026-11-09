package econest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Green-themed Swing interface for assigning orders to available delivery agents. */
public class DeliveryFrame extends JFrame {
    private final DeliveryManager manager = new DeliveryManager();
    private final JComboBox<Choice> orderCombo = new JComboBox<>();
    private final JComboBox<Choice> agentCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 14);
    private final JComboBox<String> statusCombo = new JComboBox<>(
            new String[]{"ASSIGNED", "IN_TRANSIT", "DELIVERED", "FAILED"});
    private final JLabel assignmentCount = new JLabel("0");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Delivery ID", "Order ID", "Client", "Agent ID", "Agent", "Scheduled Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public DeliveryFrame() {
        setTitle("EcoNest - Delivery Assignment (Task 6)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        buildUI();
        refreshAll();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(18, 20, 18, 20));
        root.setBackground(UITheme.BG);
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 16));
        header.setOpaque(false);
        header.add(new GreenHeader("Delivery Assignment",
                "Allocate orders to delivery agents and monitor fulfilment progress."), BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.add(statCard("Delivery records", assignmentCount, "All recorded assignments", UITheme.FOREST));
        summary.add(statCard("Assignment workflow", valueLabel("01"), "Select an order and an available agent", UITheme.LEAF));
        summary.add(statCard("Status tracking", valueLabel("LIVE"), "Update progress from the table below", UITheme.MOSS));
        header.add(summary, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(createAssignmentCard(), BorderLayout.NORTH);
        content.add(createDeliveryTableCard(), BorderLayout.CENTER);
        content.add(createActionBar(), BorderLayout.SOUTH);
        root.add(content, BorderLayout.CENTER);
    }

    private JPanel statCard(String title, JLabel value, String hint, Color accent) {
        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 7));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel label = UITheme.fieldLabel(title.toUpperCase());
        top.add(label, BorderLayout.WEST);
        JLabel marker = UITheme.badge("ECONEST", UITheme.PALE, UITheme.FOREST);
        top.add(marker, BorderLayout.EAST);
        value.setFont(UITheme.VALUE);
        value.setForeground(accent);
        JLabel description = UITheme.hint(hint);
        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(description, BorderLayout.SOUTH);
        return card;
    }

    private JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.VALUE);
        label.setForeground(UITheme.LEAF);
        return label;
    }

    private JPanel createAssignmentCard() {
        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 14));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(UITheme.sectionTitle("Create a delivery assignment"), BorderLayout.WEST);
        titlePanel.add(UITheme.hint("Choose an order, assign an agent and set the delivery date."), BorderLayout.SOUTH);
        card.add(titlePanel, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addField(fields, gbc, 0, "ORDER / CLIENT", orderCombo);
        addField(fields, gbc, 1, "AVAILABLE DELIVERY AGENT", agentCombo);
        addField(fields, gbc, 2, "SCHEDULED DATE (YYYY-MM-DD)", dateField);
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 6; buttonGbc.gridy = 0; buttonGbc.gridheight = 2;
        buttonGbc.insets = new Insets(20, 2, 0, 0);
        buttonGbc.anchor = GridBagConstraints.CENTER;
        JButton assignButton = UITheme.primaryButton("Assign Delivery  +");
        assignButton.setPreferredSize(new Dimension(170, 42));
        assignButton.addActionListener(e -> assignDelivery());
        fields.add(assignButton, buttonGbc);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int index, String label, JComponent input) {
        int x = index * 2;
        gbc.gridx = x; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(UITheme.fieldLabel(label), gbc);
        gbc.gridx = x + 1; gbc.gridy = 0; gbc.weightx = 1;
        if (input instanceof JTextField) UITheme.styleField((JTextField) input);
        if (input instanceof JComboBox) UITheme.styleCombo((JComboBox<?>) input);
        input.setPreferredSize(new Dimension(150, 40));
        panel.add(input, gbc);
    }

    private JPanel createDeliveryTableCard() {
        RoundedPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(UITheme.sectionTitle("Delivery assignments"), BorderLayout.WEST);
        title.add(UITheme.hint("Select a row to update its delivery status."), BorderLayout.SOUTH);
        card.add(title, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(UITheme.statusPillRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = UITheme.scroll(table);
        scroll.setPreferredSize(new Dimension(900, 260));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);
        JLabel label = UITheme.fieldLabel("UPDATE SELECTED DELIVERY STATUS");
        bar.add(label);
        UITheme.styleCombo(statusCombo);
        statusCombo.setPreferredSize(new Dimension(160, 38));
        bar.add(statusCombo);
        JButton update = UITheme.primaryButton("Update Status");
        update.addActionListener(e -> updateStatus());
        bar.add(update);
        JButton refresh = UITheme.secondaryButton("↻ Refresh");
        refresh.addActionListener(e -> refreshAll());
        bar.add(refresh);
        return bar;
    }

    private void assignDelivery() {
        Choice order = (Choice) orderCombo.getSelectedItem();
        Choice agent = (Choice) agentCombo.getSelectedItem();
        if (order == null || agent == null) {
            showError("No assignable order or available agent was found. Refresh the list or add an agent first.");
            return;
        }
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            if (date.isBefore(LocalDate.now())) {
                showError("The scheduled date cannot be in the past.");
                return;
            }
            manager.assignDelivery(order.id, agent.id, date);
            JOptionPane.showMessageDialog(this, "Delivery assigned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } catch (DateTimeParseException ex) {
            showError("Enter the date in YYYY-MM-DD format, for example 2026-11-20.");
        } catch (SQLException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) { showError("Select a delivery row in the table first."); return; }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String deliveryId = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String newStatus = String.valueOf(statusCombo.getSelectedItem());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Change " + deliveryId + " to " + newStatus + "?", "Confirm status update", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            manager.updateDeliveryStatus(deliveryId, newStatus);
            JOptionPane.showMessageDialog(this, "Delivery status updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } catch (SQLException | IllegalArgumentException ex) { showError(ex.getMessage()); }
    }

    private void refreshAll() {
        try {
            loadOrders(); loadAgents(); loadDeliveries();
        } catch (SQLException ex) {
            showError("Could not load delivery data. Check that MySQL is running, the EcoNest schema is imported, and config.properties is correct.\n\nDetails: " + ex.getMessage());
        }
    }

    private void loadOrders() throws SQLException {
        orderCombo.removeAllItems();
        List<String[]> orders = manager.getAssignableOrders();
        for (String[] order : orders) orderCombo.addItem(new Choice(order[0], order[0] + " — " + order[1]));
    }

    private void loadAgents() throws SQLException {
        agentCombo.removeAllItems();
        List<String[]> agents = manager.getAvailableAgents();
        for (String[] agent : agents) agentCombo.addItem(new Choice(agent[0], agent[0] + " — " + agent[1]));
    }

    private void loadDeliveries() throws SQLException {
        tableModel.setRowCount(0);
        for (Delivery delivery : manager.getAllDeliveries()) {
            tableModel.addRow(new Object[]{delivery.getDeliveryId(), delivery.getOrderId(),
                    delivery.getClientName(), delivery.getAgentId(), delivery.getAgentName(),
                    delivery.getScheduledDate(), delivery.getStatus()});
        }
        assignmentCount.setText(String.valueOf(tableModel.getRowCount()));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message == null ? "An unexpected error occurred." : message,
                "Delivery Assignment", JOptionPane.ERROR_MESSAGE);
    }

    private static class Choice {
        private final String id;
        private final String label;
        private Choice(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
