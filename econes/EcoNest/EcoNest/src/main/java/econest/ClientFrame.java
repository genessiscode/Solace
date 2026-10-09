package econest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * The client management module: add, update, remove and browse EcoNest
 * clients.
 *
 * <p>Follows the same visual structure as {@link InventoryFrame}: form on
 * top ({@link BorderLayout#NORTH}), table in the middle
 * ({@link BorderLayout#CENTER}) and buttons at the bottom
 * ({@link BorderLayout#SOUTH}). The company name field is only enabled when
 * the type combo says {@link ClientType#OFFICE}, because household clients
 * have no company.</p>
 *
 * <p>The frame closes with {@code DISPOSE_ON_CLOSE} because the dashboard
 * opens it and closing it must return there, not terminate the application.
 * Model validation errors ({@link IllegalArgumentException}) and database
 * errors ({@link IllegalStateException}) are shown in a
 * {@link JOptionPane} - no SQL failure is ever allowed to reach the
 * console.</p>
 */
public class ClientFrame extends JFrame {

    private JTextField clientIdField;
    private JTextField nameField;
    private JComboBox<ClientType> clientTypeCombo;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField companyNameField;

    private JTable clientTable;
    private DefaultTableModel tableModel;

    private ClientManager clientManager;

    /**
     * Builds the window, loads the client list and shows the frame.
     */
    public ClientFrame() {

        clientManager = new ClientManager();

        setTitle("EcoNest - Client Management");
        setSize(950, 650);
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
                new GridLayout(7, 2, 10, 10)
        );

        inputPanel.add(new JLabel("Client ID:"));

        clientIdField = new JTextField();
        inputPanel.add(clientIdField);

        inputPanel.add(new JLabel("Name:"));

        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Type:"));

        clientTypeCombo = new JComboBox<>(ClientType.values());
        inputPanel.add(clientTypeCombo);

        inputPanel.add(new JLabel("Address:"));

        addressField = new JTextField();
        inputPanel.add(addressField);

        inputPanel.add(new JLabel("Phone:"));

        phoneField = new JTextField();
        inputPanel.add(phoneField);

        inputPanel.add(new JLabel("Email:"));

        emailField = new JTextField();
        inputPanel.add(emailField);

        inputPanel.add(new JLabel("Company Name:"));

        companyNameField = new JTextField();
        inputPanel.add(companyNameField);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // ================= TABLE =================

        String[] columns = {
                "Client ID",
                "Name",
                "Type",
                "Address",
                "Phone",
                "Email",
                "Company"
        };

        tableModel = new DefaultTableModel(
                columns,
                0
        );

        clientTable = new JTable(tableModel);

        JScrollPane scrollPane =
                new JScrollPane(clientTable);

        mainPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // ================= BUTTON PANEL =================

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 5, 10, 10)
        );

        JButton addClientButton =
                new JButton("Add Client");

        JButton updateClientButton =
                new JButton("Update Client");

        JButton removeClientButton =
                new JButton("Remove Client");

        JButton clearButton =
                new JButton("Clear");

        JButton refreshButton =
                new JButton("Refresh");

        buttonPanel.add(addClientButton);
        buttonPanel.add(updateClientButton);
        buttonPanel.add(removeClientButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(mainPanel);

        // ================= TYPE SWITCH =================

        clientTypeCombo.addActionListener(e -> updateCompanyField());

        updateCompanyField();

        // ================= TABLE ROW CLICK =================

        clientTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int row =
                                clientTable.getSelectedRow();

                        if (row >= 0) {

                            clientIdField.setText(
                                    cellText(row, 0)
                            );

                            nameField.setText(
                                    cellText(row, 1)
                            );

                            clientTypeCombo.setSelectedItem(
                                    tableModel.getValueAt(row, 2)
                            );

                            addressField.setText(
                                    cellText(row, 3)
                            );

                            phoneField.setText(
                                    cellText(row, 4)
                            );

                            emailField.setText(
                                    cellText(row, 5)
                            );

                            companyNameField.setText(
                                    cellText(row, 6)
                            );
                        }
                    }
                });

        // ================= ADD CLIENT =================

        addClientButton.addActionListener(e -> {

            try {

                Client client = buildClient();

                Client existing =
                        clientManager.findClient(
                                client.getClientId()
                        );

                if (existing != null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Client ID already exists."
                    );

                    return;
                }

                clientManager.addClient(client);

                JOptionPane.showMessageDialog(
                        this,
                        "Client Added Successfully!"
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

        // ================= UPDATE CLIENT =================

        updateClientButton.addActionListener(e -> {

            try {

                Client client = buildClient();

                clientManager.updateClient(client);

                JOptionPane.showMessageDialog(
                        this,
                        "Client Updated Successfully!"
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

        // ================= REMOVE CLIENT =================

        removeClientButton.addActionListener(e -> {

            String clientId =
                    clientIdField.getText().trim();

            if (clientId.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select or enter a Client ID."
                );

                return;
            }

            int confirm =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to remove this client?",
                            "Confirm Remove",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirm
                    == JOptionPane.YES_OPTION) {

                try {

                    boolean removed =
                            clientManager.removeClient(
                                    clientId
                            );

                    if (removed) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Client Removed Successfully!"
                        );

                        updateTable();
                        clearFields();

                    } else if (clientManager.findClient(clientId) != null) {

                        JOptionPane.showMessageDialog(
                                this,
                                "Cannot remove: client still has orders."
                        );

                    } else {

                        JOptionPane.showMessageDialog(
                                this,
                                "Client not found."
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
    }

    // ================= BUILD CLIENT =================

    /**
     * Reads the form and builds a {@link Client}. The company name is only
     * sent for {@link ClientType#OFFICE} clients; households store an empty
     * company.
     *
     * @return the client typed into the form
     * @throws IllegalArgumentException if the client id or name is blank
     */
    private Client buildClient() {

        String clientId =
                clientIdField.getText().trim();

        String name =
                nameField.getText().trim();

        if (clientId.isEmpty() || name.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please enter Client ID and Name.");
        }

        ClientType clientType =
                (ClientType) clientTypeCombo.getSelectedItem();

        String companyName = "";

        if (clientType == ClientType.OFFICE) {

            companyName =
                    companyNameField.getText().trim();
        }

        return new Client(
                clientId,
                name,
                clientType,
                addressField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                companyName
        );
    }

    // ================= COMPANY FIELD =================

    /**
     * Enables the company name field only for office clients.
     */
    private void updateCompanyField() {

        companyNameField.setEnabled(
                clientTypeCombo.getSelectedItem()
                        == ClientType.OFFICE
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

            ArrayList<Client> list =
                    clientManager.getClientList();

            for (Client client : list) {

                tableModel.addRow(
                        new Object[]{
                                client.getClientId(),
                                client.getName(),
                                client.getClientType(),
                                client.getAddress(),
                                client.getPhone(),
                                client.getEmail(),
                                client.getCompanyName()
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

        clientIdField.setText("");
        nameField.setText("");
        clientTypeCombo.setSelectedIndex(0);
        addressField.setText("");
        phoneField.setText("");
        emailField.setText("");
        companyNameField.setText("");

        clientTable.clearSelection();
    }
}
