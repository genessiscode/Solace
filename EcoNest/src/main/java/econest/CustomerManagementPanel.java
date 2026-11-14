package econest.customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * EcoNest Customer Management screen.
 * Add this panel to an existing JFrame using:
 *     frame.setContentPane(new CustomerManagementPanel());
 */
public class CustomerManagementPanel extends JPanel {
    private final CustomerDAO dao = new CustomerDAO();
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> typeFilter = new JComboBox<>(new String[]{"ALL", "HOUSEHOLD", "OFFICE"});
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Customer ID", "Name", "Type", "Phone", "Email", "Company", "Address"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel countLabel = new JLabel("0 customers");

    public CustomerManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        loadCustomers();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.DARK);
        header.setBorder(new EmptyBorder(23, 28, 23, 28));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("ECONEST  /  CUSTOMER DIRECTORY");
        eyebrow.setForeground(new Color(190, 218, 195));
        eyebrow.setFont(Theme.bodyFont(Font.BOLD, 10));
        JLabel title = new JLabel("Customer Management");
        title.setForeground(Theme.WHITE);
        title.setFont(Theme.headingFont(25));
        JLabel subtitle = new JLabel("Manage household and office customer information");
        subtitle.setForeground(new Color(222, 237, 224));
        subtitle.setFont(Theme.bodyFont(Font.PLAIN, 12));
        titleBox.add(eyebrow);
        titleBox.add(Box.createVerticalStrut(7));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(subtitle);

        JButton add = Theme.button("+  Add customer", true);
        add.setBackground(new Color(91, 151, 109));
        add.setBorder(new EmptyBorder(11, 17, 11, 17));
        add.addActionListener(e -> addCustomer());
        header.add(titleBox, BorderLayout.WEST);
        header.add(add, BorderLayout.EAST);
        return header;
    }

    private JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 15));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(22, 26, 24, 26));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        searchField.setFont(Theme.bodyFont(Font.PLAIN, 13));
        searchField.setBorder(Theme.inputBorder());
        searchField.setPreferredSize(new Dimension(270, 38));
        searchField.putClientProperty("JTextField.placeholderText", "Search name, ID, phone or email...");
        JButton search = Theme.button("Search", true);
        JButton clear = Theme.button("Clear", false);
        left.add(searchField);
        left.add(Box.createHorizontalStrut(8));
        left.add(search);
        left.add(Box.createHorizontalStrut(7));
        left.add(clear);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel filterLabel = new JLabel("Customer type");
        filterLabel.setFont(Theme.bodyFont(Font.BOLD, 12));
        filterLabel.setForeground(Theme.MUTED);
        typeFilter.setFont(Theme.bodyFont(Font.PLAIN, 12));
        typeFilter.setPreferredSize(new Dimension(135, 38));
        right.add(filterLabel);
        right.add(typeFilter);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        search.addActionListener(e -> loadCustomers());
        searchField.addActionListener(e -> loadCustomers());
        typeFilter.addActionListener(e -> loadCustomers());
        clear.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedItem("ALL");
            loadCustomers();
        });

        JPanel tableCard = new JPanel(new BorderLayout(0, 0));
        tableCard.setBackground(Theme.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(12, 12, 12, 12)));

        table.setRowHeight(42);
        table.setFont(Theme.bodyFont(Font.PLAIN, 12));
        table.setForeground(Theme.TEXT);
        table.setGridColor(new Color(237, 241, 237));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(Theme.MINT);
        table.setSelectionForeground(Theme.DARK);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JTableHeader th = table.getTableHeader();
        th.setFont(Theme.bodyFont(Font.BOLD, 11));
        th.setForeground(Theme.DARK);
        th.setBackground(new Color(239, 246, 240));
        th.setPreferredSize(new Dimension(0, 39));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        countLabel.setFont(Theme.bodyFont(Font.PLAIN, 11));
        countLabel.setForeground(Theme.MUTED);
        footer.add(countLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refresh = Theme.button("Refresh", false);
        JButton edit = Theme.button("Edit selected", false);
        JButton delete = Theme.button("Delete selected", false);
        delete.setForeground(Theme.RED);
        refresh.addActionListener(e -> loadCustomers());
        edit.addActionListener(e -> editCustomer());
        delete.addActionListener(e -> deleteCustomer());
        actions.add(refresh);
        actions.add(edit);
        actions.add(delete);
        footer.add(actions, BorderLayout.EAST);

        main.add(toolbar, BorderLayout.NORTH);
        main.add(tableCard, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);
        return main;
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = dao.getAll(searchField.getText(), (String) typeFilter.getSelectedItem());
            model.setRowCount(0);
            for (Customer c : customers) {
                model.addRow(new Object[]{c.getClientId(), c.getName(), c.getClientType(),
                    c.getPhone(), c.getEmail(), c.getCompanyName(), c.getAddress()});
            }
            countLabel.setText(customers.size() + (customers.size() == 1 ? " customer" : " customers"));
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private Customer selectedCustomer() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table.", "Select customer", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int row = table.convertRowIndexToModel(viewRow);
        String id = String.valueOf(model.getValueAt(row, 0));
        try {
            for (Customer c : dao.getAll("", "ALL")) if (c.getClientId().equals(id)) return c;
        } catch (SQLException ex) { showDatabaseError(ex); }
        return null;
    }

    private void addCustomer() {
        CustomerFormDialog dialog = new CustomerFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        Customer c = dialog.getCustomer();
        if (c == null) return;
        try {
            String id = dao.create(c);
            loadCustomers();
            JOptionPane.showMessageDialog(this, "Customer added successfully.\nCustomer ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void editCustomer() {
        Customer old = selectedCustomer();
        if (old == null) return;
        CustomerFormDialog dialog = new CustomerFormDialog(SwingUtilities.getWindowAncestor(this), old);
        dialog.setVisible(true);
        Customer changed = dialog.getCustomer();
        if (changed == null) return;
        Customer updated = new Customer(old.getClientId(), changed.getName(), changed.getClientType(),
            changed.getAddress(), changed.getPhone(), changed.getEmail(), changed.getCompanyName());
        try {
            dao.update(updated);
            loadCustomers();
            JOptionPane.showMessageDialog(this, "Customer details updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void deleteCustomer() {
        Customer c = selectedCustomer();
        if (c == null) return;
        int answer = JOptionPane.showConfirmDialog(this,
            "Delete " + c.getName() + " (" + c.getClientId() + ")?\nThis cannot be undone.",
            "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(c.getClientId());
            loadCustomers();
            JOptionPane.showMessageDialog(this, "Customer deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(this,
                    "This customer has existing orders and cannot be deleted.\nKeep the customer record to preserve order history.",
                    "Customer is linked to orders", JOptionPane.WARNING_MESSAGE);
            } else showDatabaseError(ex);
        }
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Could not complete the database operation.\n\n" + ex.getMessage() +
            "\n\nCheck the MySQL server, database name, username/password and JDBC driver.",
            "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
