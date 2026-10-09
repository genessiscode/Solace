package econest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The delivery agent management module: add, update, remove and browse
 * EcoNest agents together with their vehicles.
 *
 * <p>Follows the same visual structure as {@link InventoryFrame}: form on
 * top ({@link BorderLayout#NORTH}), table in the middle
 * ({@link BorderLayout#CENTER}) and buttons at the bottom
 * ({@link BorderLayout#SOUTH}). One form row holds the agent, the next
 * four hold the agent's vehicle - a vehicle is only saved when both the
 * vehicle id and the plate number are filled in, and clearing both
 * removes the agent's vehicle on update.</p>
 *
 * <p>The frame closes with {@code DISPOSE_ON_CLOSE} because the dashboard
 * opens it and closing it must return there, not terminate the application.
 * Model validation errors ({@link IllegalArgumentException}) and database
 * errors ({@link IllegalStateException}) are shown in a
 * {@link JOptionPane} - no SQL failure is ever allowed to reach the
 * console.</p>
 */
public class AgentFrame extends JFrame {

    private JTextField agentIdField;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<AgentStatus> statusCombo;
    private JTextField vehicleIdField;
    private JComboBox<VehicleType> vehicleTypeCombo;
    private JTextField plateNumberField;
    private JTextField capacityField;

    private JTable agentTable;
    private DefaultTableModel tableModel;

    private JTextField searchIdField;

    private AgentManager agentManager;

    /**
     * Builds the window, loads the agent list and shows the frame.
     */
    public AgentFrame() {

        agentManager = new AgentManager();

        setTitle("EcoNest - Delivery Agent Management");
        setSize(1000, 650);
        // DISPOSE_ON_CLOSE, not EXIT_ON_CLOSE: the dashboard opens this frame
        // and closing it must return there, not terminate the application.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
                new GridLayout(9, 2, 10, 10)
        );

        inputPanel.add(new JLabel("Agent ID:"));

        agentIdField = new JTextField();
        inputPanel.add(agentIdField);

        inputPanel.add(new JLabel("Name:"));

        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Phone:"));

        phoneField = new JTextField();
        inputPanel.add(phoneField);

        inputPanel.add(new JLabel("Email:"));

        emailField = new JTextField();
        inputPanel.add(emailField);

        inputPanel.add(new JLabel("Status:"));

        statusCombo = new JComboBox<>(AgentStatus.values());
        inputPanel.add(statusCombo);

        inputPanel.add(new JLabel("Vehicle ID:"));

        vehicleIdField = new JTextField();
        inputPanel.add(vehicleIdField);

        inputPanel.add(new JLabel("Vehicle Type:"));

        vehicleTypeCombo = new JComboBox<>(VehicleType.values());
        inputPanel.add(vehicleTypeCombo);

        inputPanel.add(new JLabel("Plate Number:"));

        plateNumberField = new JTextField();
        inputPanel.add(plateNumberField);

        inputPanel.add(new JLabel("Capacity (kg):"));

        capacityField = new JTextField();
        inputPanel.add(capacityField);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // ================= SEARCH PANEL =================
        // Ported from the submitted Task 4 form: lets the user pull one
        // agent back by id instead of scrolling the whole table.

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        searchPanel.add(new JLabel("Search Agent ID:"));

        searchIdField = new JTextField(15);
        searchPanel.add(searchIdField);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        JButton showAllButton = new JButton("Show All");
        searchPanel.add(showAllButton);

        // ================= TABLE =================

        String[] columns = {
                "Agent ID",
                "Name",
                "Phone",
                "Email",
                "Status",
                "Vehicle ID",
                "Vehicle Type",
                "Plate Number",
                "Capacity (kg)"
        };

        tableModel = new DefaultTableModel(
                columns,
                0
        );

        agentTable = new JTable(tableModel);

        // Clicking a column header sorts, matching the submitted Task 4 form.
        agentTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane =
                new JScrollPane(agentTable);

        // Search row sits directly above the table it filters.
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(
                tablePanel,
                BorderLayout.CENTER
        );

        // ================= BUTTON PANEL =================

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 5, 10, 10)
        );

        JButton addAgentButton =
                new JButton("Add Agent");

        JButton updateAgentButton =
                new JButton("Update Agent");

        JButton removeAgentButton =
                new JButton("Remove Agent");

        JButton clearButton =
                new JButton("Clear");

        JButton refreshButton =
                new JButton("Refresh");

        buttonPanel.add(addAgentButton);
        buttonPanel.add(updateAgentButton);
        buttonPanel.add(removeAgentButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        // ================= TABLE ROW CLICK =================

        agentTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int row =
                                agentTable.getSelectedRow();

                        if (row >= 0) {

                            agentIdField.setText(
                                    cellText(row, 0)
                            );

                            nameField.setText(
                                    cellText(row, 1)
                            );

                            phoneField.setText(
                                    cellText(row, 2)
                            );

                            emailField.setText(
                                    cellText(row, 3)
                            );

                            statusCombo.setSelectedItem(
                                    AgentStatus.valueOf(
                                            cellText(row, 4))
                            );

                            // Vehicle columns are empty when the agent
                            // has no vehicle - clear those fields then.
                            String vehicleId =
                                    cellText(row, 5);

                            vehicleIdField.setText(
                                    vehicleId
                            );

                            if (vehicleId.isEmpty()) {

                                vehicleTypeCombo.setSelectedIndex(0);
                                plateNumberField.setText("");
                                capacityField.setText("");

                            } else {

                                vehicleTypeCombo.setSelectedItem(
                                        VehicleType.valueOf(
                                                cellText(row, 6))
                                );

                                plateNumberField.setText(
                                        cellText(row, 7)
                                );

                                capacityField.setText(
                                        cellText(row, 8)
                                );
                            }
                        }
                    }
                });

        // ================= ADD AGENT =================

        addAgentButton.addActionListener(e -> {

            try {

                Agent agent =
                        buildAgent();

                Agent existing =
                        agentManager.findAgent(
                                agent.getAgentId()
                        );

                if (existing != null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Agent ID already exists."
                    );

                    return;
                }

                agentManager.addAgent(agent);

                Vehicle vehicle =
                        buildVehicle();

                if (vehicle != null) {

                    agentManager.addVehicle(vehicle);
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Agent Added Successfully!"
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

        // ================= UPDATE AGENT =================

        updateAgentButton.addActionListener(e -> {

            try {

                Agent agent =
                        buildAgent();

                agentManager.updateAgent(agent);

                Vehicle vehicle =
                        buildVehicle();

                Vehicle existing =
                        agentManager.findVehicleByAgent(
                                agent.getAgentId()
                        );

                if (vehicle == null) {

                    // Vehicle fields cleared - drop the old vehicle.
                    if (existing != null) {

                        agentManager.removeVehicle(
                                existing.getVehicleId()
                        );
                    }

                } else if (existing == null) {

                    agentManager.addVehicle(vehicle);

                } else if (existing.getVehicleId()
                        .equals(vehicle.getVehicleId())) {

                    agentManager.updateVehicle(vehicle);

                } else {

                    // The agent swapped vehicles - replace the row.
                    agentManager.removeVehicle(
                            existing.getVehicleId()
                    );

                    agentManager.addVehicle(vehicle);
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Agent Updated Successfully!"
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

        // ================= REMOVE AGENT =================

        removeAgentButton.addActionListener(e -> {

            String agentId =
                    agentIdField.getText().trim();

            if (agentId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select or enter an Agent ID."
                );

                return;
            }

            int confirm =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to remove this agent?",
                            "Confirm Remove",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirm
                    == JOptionPane.YES_OPTION) {

                try {

                    boolean removed =
                            agentManager.removeAgent(
                                    agentId
                            );

                    if (removed) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Agent Removed Successfully!"
                        );

                        updateTable();
                        clearFields();

                    } else if (agentManager.findAgent(agentId) != null) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Cannot remove: agent has delivery history."
                        );

                    } else {

                        JOptionPane.showMessageDialog(
                                this,
                                "Agent not found."
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

        // ================= CLEAR =================

        clearButton.addActionListener(e -> {

            clearFields();

        });

        // ================= REFRESH =================

        refreshButton.addActionListener(e -> {

            updateTable();

        });

        // ================= SEARCH =================

        searchButton.addActionListener(e -> {

            String agentId =
                    searchIdField.getText().trim();

            if (agentId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter an Agent ID to search."
                );

                return;
            }

            try {

                Agent found = agentManager.findAgent(agentId);

                if (found == null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "No agent found with ID: " + agentId
                    );

                    return;
                }

                // Fill the form so the agent can be edited straight away.
                agentIdField.setText(found.getAgentId());
                nameField.setText(found.getName());
                phoneField.setText(found.getPhone());
                emailField.setText(found.getEmail());
                statusCombo.setSelectedItem(found.getStatus());

                Vehicle vehicle =
                        agentManager.findVehicleByAgent(agentId);

                if (vehicle == null) {

                    vehicleIdField.setText("");
                    vehicleTypeCombo.setSelectedIndex(0);
                    plateNumberField.setText("");
                    capacityField.setText("");

                } else {

                    vehicleIdField.setText(vehicle.getVehicleId());
                    vehicleTypeCombo.setSelectedItem(vehicle.getType());
                    plateNumberField.setText(vehicle.getPlateNumber());
                    capacityField.setText(
                            Double.isNaN(vehicle.getCapacityKg())
                                    ? ""
                                    : String.format(
                                            Locale.ROOT,
                                            "%.2f",
                                            vehicle.getCapacityKg())
                    );
                }

                selectRow(agentId);

            } catch (IllegalStateException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage()
                );
            }
        });

        // ================= SHOW ALL =================

        showAllButton.addActionListener(e -> {

            searchIdField.setText("");

            updateTable();

        });
    }

    /**
     * Highlights the table row for one agent and scrolls it into view, so a
     * search result is visible rather than only loaded into the form.
     *
     * @param agentId the agent to select
     */
    private void selectRow(String agentId) {

        for (int row = 0; row < tableModel.getRowCount(); row++) {

            if (agentId.equals(cellText(row, 0))) {

                // Convert the model row to a view row: the table is
                // sortable, so the two indexes differ once a header is
                // clicked.
                int viewRow = agentTable.convertRowIndexToView(row);

                agentTable.setRowSelectionInterval(viewRow, viewRow);
                agentTable.scrollRectToVisible(
                        agentTable.getCellRect(viewRow, 0, true));

                return;
            }
        }
    }

    // ================= BUILD AGENT =================

    /**
     * Reads the agent half of the form and builds an {@link Agent}.
     *
     * @return the agent typed into the form
     * @throws IllegalArgumentException if the agent id or name is blank, or
     *                                  the phone number is not 10 digits
     */
    private Agent buildAgent() {

        String agentId =
                agentIdField.getText().trim();

        String name =
                nameField.getText().trim();

        if (agentId.isEmpty() || name.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please enter Agent ID and Name.");
        }

        String phone =
                phoneField.getText().trim();

        // Ported from the submitted Task 4 form, which enforced a 10-digit
        // Sri Lankan number. A blank phone stays optional.
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

    /**
     * Reads the vehicle half of the form and builds a {@link Vehicle}.
     *
     * <p>Returns {@code null} when both the vehicle id and the plate
     * number are empty, meaning "this agent has no vehicle". A blank
     * capacity is stored as {@code NaN}, which the manager writes as SQL
     * {@code NULL}.</p>
     *
     * @return the vehicle typed into the form, or {@code null} when the
     *         vehicle fields are cleared
     * @throws IllegalArgumentException if only one of the vehicle id and
     *                                  plate number is filled in, or the
     *                                  capacity is not a number
     */
    private Vehicle buildVehicle() {

        String vehicleId =
                vehicleIdField.getText().trim();

        String plateNumber =
                plateNumberField.getText().trim();

        if (vehicleId.isEmpty() && plateNumber.isEmpty()) {

            return null;
        }

        if (vehicleId.isEmpty() || plateNumber.isEmpty()) {

            throw new IllegalArgumentException(
                    "Enter both Vehicle ID and Plate Number,"
                            + " or leave both empty.");
        }

        String capacityText =
                capacityField.getText().trim();

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

    /** Reads one table cell as text, never {@code null}. */
    private String cellText(int row, int column) {

        Object value = tableModel.getValueAt(row, column);

        return value == null ? "" : value.toString();
    }

    // ================= UPDATE TABLE =================

    private void updateTable() {

        tableModel.setRowCount(0);

        try {

            ArrayList<AgentManager.AgentRow> rows =
                    agentManager.getAgentTableList("2026-10-17");

            for (AgentManager.AgentRow row : rows) {

                tableModel.addRow(
                        new Object[]{
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

    // ================= CLEAR FIELDS =================

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

        searchIdField.setText("");

        agentTable.clearSelection();
    }
}
