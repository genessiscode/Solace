package econest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Swing interface for assigning orders to available delivery agents. */
public class DeliveryFrame extends JFrame {
    private final DeliveryManager manager = new DeliveryManager();
    private final JComboBox<Choice> orderCombo = new JComboBox<>();
    private final JComboBox<Choice> agentCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ASSIGNED", "IN_TRANSIT", "DELIVERED", "FAILED"});
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Delivery ID", "Order ID", "Client", "Agent ID", "Agent", "Scheduled Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public DeliveryFrame() {
        setTitle("EcoNest - Delivery Assignment (Task 6)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 620);
        setLocationRelativeTo(null);
        buildUI();
        refreshAll();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(247, 249, 246));
        setContentPane(root);

        JLabel heading = new JLabel("Delivery Assignment");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(new Color(36, 83, 59));
        JLabel subtitle = new JLabel("Assign orders to available agents and track delivery progress.");
        subtitle.setForeground(new Color(90, 100, 92));
        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headingPanel.setOpaque(false);
        headingPanel.add(heading);
        headingPanel.add(subtitle);

        JPanel assignmentPanel = new JPanel(new GridBagLayout());
        assignmentPanel.setBorder(BorderFactory.createTitledBorder("Create Assignment"));
        assignmentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addField(assignmentPanel, gbc, 0, "Order / Client", orderCombo);
        addField(assignmentPanel, gbc, 1, "Available Agent", agentCombo);
        addField(assignmentPanel, gbc, 2, "Scheduled date (YYYY-MM-DD)", dateField);
        JButton assignButton = new JButton("Assign Delivery");
        assignButton.setBackground(new Color(43, 105, 72));
        assignButton.setForeground(Color.WHITE);
        assignButton.setFocusPainted(false);
        assignButton.addActionListener(e -> assignDelivery());
        gbc.gridx = 6; gbc.gridy = 0; gbc.gridheight = 2; gbc.weightx = 0;
        assignmentPanel.add(assignButton, gbc);

        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Delivery Assignments"));

        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        updatePanel.setOpaque(false);
        updatePanel.add(new JLabel("Update selected delivery status:"));
        updatePanel.add(statusCombo);
        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> updateStatus());
        updatePanel.add(updateButton);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAll());
        updatePanel.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(headingPanel, BorderLayout.NORTH);
        top.add(assignmentPanel, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(updatePanel, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int index, String label, JComponent input) {
        gbc.gridheight = 1; gbc.weightx = 0; gbc.gridx = index * 2; gbc.gridy = 0;
        panel.add(new JLabel(label), gbc);
        gbc.weightx = 1; gbc.gridx = index * 2 + 1;
        panel.add(input, gbc);
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
            showError("Enter the date in YYYY-MM-DD format, for example 2026-10-20.");
        } catch (SQLException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Select a delivery row in the table first.");
            return;
        }
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
        } catch (SQLException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAll() {
        try {
            loadOrders();
            loadAgents();
            loadDeliveries();
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
