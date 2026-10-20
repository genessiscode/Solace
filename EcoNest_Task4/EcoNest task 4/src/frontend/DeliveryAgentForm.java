package frontend;

import backend.DeliveryAgentService;
import model.DeliveryAgent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DeliveryAgentForm extends JFrame {

    private final DeliveryAgentService service = new DeliveryAgentService();

    private final JTextField txtAgentId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtVehicleNumber = new JTextField();
    private final JComboBox<String> cmbVehicleType =
            new JComboBox<>(new String[]{"Van", "Car", "Motorcycle"});
    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{"Available", "Unavailable"});

    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Agent ID", "Name", "Phone", "Address",
                         "Vehicle Number", "Vehicle Type", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public DeliveryAgentForm() {
        setTitle("EcoNest - Manage Delivery Agents");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        buildUI();
        loadAgents();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Manage Delivery Agents", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        main.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Delivery Agent Details"));

        addField(formPanel, "Agent ID:", txtAgentId, 0);
        addField(formPanel, "Name:", txtName, 1);
        addField(formPanel, "Phone:", txtPhone, 2);
        addField(formPanel, "Address:", txtAddress, 3);
        addField(formPanel, "Vehicle Number:", txtVehicleNumber, 4);
        addField(formPanel, "Vehicle Type:", cmbVehicleType, 5);
        addField(formPanel, "Status:", cmbStatus, 6);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        JPanel topCenter = new JPanel(new BorderLayout(5, 8));
        topCenter.add(formPanel, BorderLayout.CENTER);
        topCenter.add(buttons, BorderLayout.SOUTH);

        main.add(topCenter, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Delivery Agents"));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search Agent ID:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        searchPanel.add(btnSearch, BorderLayout.EAST);
        searchPanel.add(btnShowAll, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(topCenter, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);
        main.remove(topCenter);
        main.add(content, BorderLayout.CENTER);

        setContentPane(main);

        btnAdd.addActionListener(e -> addAgent());
        btnUpdate.addActionListener(e -> updateAgent());
        btnDelete.addActionListener(e -> deleteAgent());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchAgent());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadAgents();
        });

        txtSearch.addActionListener(e -> searchAgent());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRow();
            }
        });
    }

    private void addField(JPanel panel, String label, JComponent component, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private boolean validateInput() {
        if (txtAgentId.getText().trim().isEmpty() ||
            txtName.getText().trim().isEmpty() ||
            txtPhone.getText().trim().isEmpty() ||
            txtAddress.getText().trim().isEmpty() ||
            txtVehicleNumber.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (!txtPhone.getText().trim().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                    "Phone number must contain exactly 10 digits.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private DeliveryAgent getAgentFromForm() {
        return new DeliveryAgent(
                txtAgentId.getText().trim(),
                txtName.getText().trim(),
                txtPhone.getText().trim(),
                txtAddress.getText().trim(),
                txtVehicleNumber.getText().trim(),
                cmbVehicleType.getSelectedItem().toString(),
                cmbStatus.getSelectedItem().toString()
        );
    }

    private void addAgent() {
        if (!validateInput()) return;

        try {
            if (service.searchAgent(txtAgentId.getText().trim()) != null) {
                JOptionPane.showMessageDialog(this,
                        "Agent ID already exists.",
                        "Duplicate ID",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (service.addAgent(getAgentFromForm())) {
                JOptionPane.showMessageDialog(this,
                        "Delivery agent added successfully.");
                clearForm();
                loadAgents();
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateAgent() {
        if (!validateInput()) return;

        try {
            if (service.updateAgent(getAgentFromForm())) {
                JOptionPane.showMessageDialog(this,
                        "Delivery agent updated successfully.");
                clearForm();
                loadAgents();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Agent ID was not found.",
                        "Update Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteAgent() {
        String id = txtAgentId.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Select an agent or enter an Agent ID first.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete agent " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            if (service.deleteAgent(id)) {
                JOptionPane.showMessageDialog(this,
                        "Delivery agent deleted successfully.");
                clearForm();
                loadAgents();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Agent ID was not found.",
                        "Delete Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void searchAgent() {
        String id = txtSearch.getText().trim();

        if (id.isEmpty()) {
            loadAgents();
            return;
        }

        try {
            DeliveryAgent agent = service.searchAgent(id);
            tableModel.setRowCount(0);

            if (agent != null) {
                addAgentToTable(agent);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No delivery agent found with ID: " + id);
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void loadAgents() {
        try {
            List<DeliveryAgent> agents = service.getAllAgents();
            tableModel.setRowCount(0);

            for (DeliveryAgent agent : agents) {
                addAgentToTable(agent);
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void addAgentToTable(DeliveryAgent agent) {
        tableModel.addRow(new Object[]{
                agent.getAgentId(),
                agent.getName(),
                agent.getPhone(),
                agent.getAddress(),
                agent.getVehicleNumber(),
                agent.getVehicleType(),
                agent.getStatus()
        });
    }

    private void loadSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;

        int row = table.convertRowIndexToModel(viewRow);

        txtAgentId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        txtPhone.setText(tableModel.getValueAt(row, 2).toString());
        txtAddress.setText(tableModel.getValueAt(row, 3).toString());
        txtVehicleNumber.setText(tableModel.getValueAt(row, 4).toString());
        cmbVehicleType.setSelectedItem(tableModel.getValueAt(row, 5).toString());
        cmbStatus.setSelectedItem(tableModel.getValueAt(row, 6).toString());

        txtAgentId.setEditable(false);
    }

    private void clearForm() {
        txtAgentId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtVehicleNumber.setText("");
        cmbVehicleType.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
        txtAgentId.setEditable(true);
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Database error:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
