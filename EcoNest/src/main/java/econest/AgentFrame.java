package econest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Delivery agent management: add, update, remove and browse agents and the
 * vehicles assigned to them.
 */
public class AgentFrame extends JFrame {

    private final AgentManager agentManager = new AgentManager();

    private final JTextField agentIdField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JComboBox<AgentStatus> statusCombo =
            new JComboBox<>(AgentStatus.values());
    private final JTextField vehicleIdField = new JTextField();
    private final JComboBox<VehicleType> vehicleTypeCombo =
            new JComboBox<>(VehicleType.values());
    private final JTextField plateNumberField = new JTextField();
    private final JTextField capacityField = new JTextField();
    private final JTextField searchField = new JTextField(16);

    private final StatCard agentCount =
            new StatCard("Delivery agents", UITheme.FOREST);
    private final StatCard vehicleCount =
            new StatCard("Vehicles", UITheme.LEAF);
    private final StatCard availableCount =
            new StatCard("Available now", UITheme.MOSS);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                    "Agent ID", "Name", "Phone", "Email", "Status",
                    "Vehicle ID", "Type", "Plate Number", "Capacity (kg)"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public AgentFrame() {

        setTitle("EcoNest - Delivery Agents (Task 4)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

        buildUI();
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
        header.add(new GreenHeader("Delivery Agents",
                        "Manage delivery personnel and the vehicles they use."),
                BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.add(agentCount);
        summary.add(vehicleCount);
        summary.add(availableCount);
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
        title.add(UITheme.sectionTitle("Agent and vehicle details"),
                BorderLayout.WEST);
        title.add(UITheme.hint(
                        "Leave both vehicle ID and plate number empty when the"
                                + " agent has no vehicle."),
                BorderLayout.SOUTH);
        card.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(fields, gbc, 0, 0, "AGENT ID", agentIdField);
        addField(fields, gbc, 1, 0, "NAME", nameField);
        addField(fields, gbc, 2, 0, "STATUS", statusCombo);

        addField(fields, gbc, 0, 1, "PHONE", phoneField);
        addField(fields, gbc, 1, 1, "EMAIL", emailField);
        addField(fields, gbc, 2, 1, "VEHICLE ID", vehicleIdField);

        addField(fields, gbc, 0, 2, "VEHICLE TYPE", vehicleTypeCombo);
        addField(fields, gbc, 1, 2, "PLATE NUMBER", plateNumberField);
        addField(fields, gbc, 2, 2, "CAPACITY (KG)", capacityField);

        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int column,
            int row,
            String label,
            JComponent input) {

        int x = column * 2;

        gbc.gridx = x;
        gbc.gridy = row;
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
        title.add(UITheme.sectionTitle("Delivery agents"), BorderLayout.WEST);
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
            refreshTable();
        });
        searchBar.add(showAllButton);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(4)
                .setCellRenderer(UITheme.statusPillRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    loadSelectedRow(table.convertRowIndexToModel(row));
                }
            }
        });

        JScrollPane scroll = UITheme.scroll(table);
        scroll.setPreferredSize(new Dimension(900, 250));

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(searchBar, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActionBar() {

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton addButton = UITheme.primaryButton("Add Agent  +");
        addButton.addActionListener(e -> addAgent());
        bar.add(addButton);

        JButton updateButton = UITheme.secondaryButton("Update Agent");
        updateButton.addActionListener(e -> updateAgent());
        bar.add(updateButton);

        JButton removeButton = UITheme.dangerButton("Remove Agent");
        removeButton.addActionListener(e -> removeAgent());
        bar.add(removeButton);

        JButton clearButton = UITheme.ghostButton("Clear");
        clearButton.addActionListener(e -> clearFields());
        bar.add(clearButton);

        JButton refreshButton = UITheme.secondaryButton("↻ Refresh");
        refreshButton.addActionListener(e -> refreshAll());
        bar.add(refreshButton);

        return bar;
    }

    // ================= ACTIONS =================

    private void addAgent() {

        try {
            Agent agent = buildAgent();

            if (agentManager.findAgent(agent.getAgentId()) != null) {
                showError("Agent ID already exists.");
                return;
            }

            agentManager.addAgent(agent);

            Vehicle vehicle = buildVehicle();
            if (vehicle != null) {
                agentManager.addVehicle(vehicle);
            }

            showInfo("Agent added successfully.");
            refreshAll();
            clearFields();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateAgent() {

        try {
            Agent agent = buildAgent();
            agentManager.updateAgent(agent);

            Vehicle vehicle = buildVehicle();
            Vehicle existing = agentManager.findVehicleByAgent(
                    agent.getAgentId());

            if (vehicle == null) {
                if (existing != null) {
                    agentManager.removeVehicle(existing.getVehicleId());
                }
            } else if (existing == null) {
                agentManager.addVehicle(vehicle);
            } else if (existing.getVehicleId().equals(
                    vehicle.getVehicleId())) {
                agentManager.updateVehicle(vehicle);
            } else {
                agentManager.removeVehicle(existing.getVehicleId());
                agentManager.addVehicle(vehicle);
            }

            showInfo("Agent updated successfully.");
            refreshAll();
            clearFields();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeAgent() {

        String agentId = agentIdField.getText().trim();

        if (agentId.isEmpty()) {
            showError("Select an agent or enter an Agent ID first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove agent " + agentId + " and its vehicle?",
                "Confirm remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean removed = agentManager.removeAgent(agentId);

            if (removed) {
                showInfo("Agent removed successfully.");
                refreshAll();
                clearFields();
            } else if (agentManager.findAgent(agentId) != null) {
                showError("Cannot remove: this agent has delivery history.");
            } else {
                showError("Agent not found.");
            }

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
            ArrayList<AgentManager.AgentRow> rows =
                    agentManager.getAgentTableList();

            for (AgentManager.AgentRow row : rows) {

                if (!query.isEmpty() && !matches(row, query)) {
                    continue;
                }

                tableModel.addRow(new Object[]{
                        row.getAgentId(),
                        row.getName(),
                        row.getPhone(),
                        row.getEmail(),
                        row.getStatus(),
                        row.getVehicleId(),
                        row.getVehicleType(),
                        row.getPlateNumber(),
                        row.getCapacityKg() == null
                                ? ""
                                : String.format(
                                        Locale.ROOT,
                                        "%.2f",
                                        row.getCapacityKg())
                });
            }

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean matches(AgentManager.AgentRow row, String query) {
        return contains(row.getAgentId(), query)
                || contains(row.getName(), query)
                || contains(row.getPhone(), query)
                || contains(row.getEmail(), query)
                || contains(row.getPlateNumber(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void updateSummary() {

        try {
            ArrayList<AgentManager.AgentRow> rows =
                    agentManager.getAgentTableList();

            int vehicles = 0;
            int available = 0;

            for (AgentManager.AgentRow row : rows) {
                if (row.getVehicleId() != null) {
                    vehicles++;
                }
                if (row.getStatus() == AgentStatus.AVAILABLE) {
                    available++;
                }
            }

            agentCount.setValue(rows.size());
            vehicleCount.setValue(vehicles);
            availableCount.setValue(available);

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    // ================= FORM =================

    private Agent buildAgent() {

        String agentId = agentIdField.getText().trim();
        String name = nameField.getText().trim();

        if (agentId.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter Agent ID and Name.");
        }

        String phone = phoneField.getText().trim();

        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Phone number must be exactly 10 digits.");
        }

        return new Agent(
                agentId,
                name,
                phone,
                emailField.getText().trim(),
                (AgentStatus) statusCombo.getSelectedItem()
        );
    }

    private Vehicle buildVehicle() {

        String vehicleId = vehicleIdField.getText().trim();
        String plateNumber = plateNumberField.getText().trim();

        if (vehicleId.isEmpty() && plateNumber.isEmpty()) {
            return null;
        }

        if (vehicleId.isEmpty() || plateNumber.isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter both Vehicle ID and Plate Number,"
                            + " or leave both empty.");
        }

        String capacityText = capacityField.getText().trim();
        double capacityKg = Double.NaN;

        if (!capacityText.isEmpty()) {
            try {
                capacityKg = Double.parseDouble(capacityText);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Please enter a valid capacity in kg.");
            }
        }

        return new Vehicle(
                vehicleId,
                agentIdField.getText().trim(),
                (VehicleType) vehicleTypeCombo.getSelectedItem(),
                plateNumber,
                capacityKg
        );
    }

    private void loadSelectedRow(int row) {

        agentIdField.setText(cellText(row, 0));
        nameField.setText(cellText(row, 1));
        phoneField.setText(cellText(row, 2));
        emailField.setText(cellText(row, 3));
        statusCombo.setSelectedItem(AgentStatus.valueOf(cellText(row, 4)));

        String vehicleId = cellText(row, 5);
        vehicleIdField.setText(vehicleId);

        if (vehicleId.isEmpty()) {
            vehicleTypeCombo.setSelectedIndex(0);
            plateNumberField.setText("");
            capacityField.setText("");
        } else {
            vehicleTypeCombo.setSelectedItem(
                    VehicleType.valueOf(cellText(row, 6)));
            plateNumberField.setText(cellText(row, 7));
            capacityField.setText(cellText(row, 8));
        }
    }

    private void clearFields() {

        agentIdField.setText("");
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        statusCombo.setSelectedIndex(0);
        vehicleIdField.setText("");
        vehicleTypeCombo.setSelectedIndex(0);
        plateNumberField.setText("");
        capacityField.setText("");
        searchField.setText("");

        table.clearSelection();
    }

    private String cellText(int modelRow, int column) {
        Object value = tableModel.getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Delivery Agents",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null ? "An unexpected error occurred." : message,
                "Delivery Agents",
                JOptionPane.ERROR_MESSAGE);
    }
}
