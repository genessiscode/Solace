package econest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Pattern;

final class CustomerFormDialog extends JDialog {
    private final JTextField nameField = new JTextField(24);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"HOUSEHOLD", "OFFICE"});
    private final JTextField companyField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextArea addressArea = new JTextArea(3, 24);
    private Customer result;

    CustomerFormDialog(Window owner, Customer existing) {
        super(owner, existing == null ? "Add Customer" : "Edit Customer", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        if (existing != null) fill(existing);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    Customer getCustomer() { return result; }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(22, 24, 20, 24));

        JLabel heading = new JLabel(getTitle());
        heading.setFont(UITheme.H2);
        heading.setForeground(UITheme.DEEP);
        root.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 0;
        addRow(fields, gc, "Customer name *", nameField);
        addRow(fields, gc, "Customer type *", typeBox);
        addRow(fields, gc, "Company name", companyField);
        addRow(fields, gc, "Phone", phoneField);
        addRow(fields, gc, "Email", emailField);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(UITheme.BODY);
        addressArea.setForeground(UITheme.TEXT);
        addressArea.setBackground(UITheme.CARD);
        addressArea.setCaretColor(UITheme.FOREST);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(UITheme.BORDER, 10, 1),
            new EmptyBorder(7, 10, 7, 10)));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setPreferredSize(new Dimension(280, 70));
        addRow(fields, gc, "Address", addressScroll);
        root.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        actions.setOpaque(false);
        JButton cancel = UITheme.secondaryButton("Cancel");
        JButton save = UITheme.primaryButton("Save customer");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> save());
        actions.add(cancel);
        actions.add(save);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void addRow(JPanel panel, GridBagConstraints gc, String label, JComponent input) {
        gc.gridy = panel.getComponentCount() / 2;
        gc.gridx = 0; gc.weightx = 0;
        JLabel l = UITheme.fieldLabel(label);
        panel.add(l, gc);
        gc.gridx = 1; gc.weightx = 1; gc.insets = new Insets(6, 8, 6, 0);
        if (input instanceof JTextField) {
            input.setPreferredSize(new Dimension(280, 36));
            UITheme.styleField((JTextField) input);
        } else if (input instanceof JComboBox) {
            input.setPreferredSize(new Dimension(280, 36));
            UITheme.styleCombo((JComboBox<?>) input);
        }
        panel.add(input, gc);
        gc.insets = new Insets(6, 0, 6, 10);
    }

    private void fill(Customer c) {
        nameField.setText(c.getName());
        typeBox.setSelectedItem(c.getClientType());
        companyField.setText(c.getCompanyName() == null ? "" : c.getCompanyName());
        phoneField.setText(c.getPhone() == null ? "" : c.getPhone());
        emailField.setText(c.getEmail() == null ? "" : c.getEmail());
        addressArea.setText(c.getAddress() == null ? "" : c.getAddress());
    }

    private void save() {
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the customer name.", "Validation", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        if (!email.isEmpty() && !Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }
        if (!phone.isEmpty() && !phone.matches("[0-9+() -]{7,20}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number.", "Validation", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }
        String company = companyField.getText().trim();
        if ("OFFICE".equals(type) && company.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the company name for an OFFICE customer.", "Validation", JOptionPane.WARNING_MESSAGE);
            companyField.requestFocus();
            return;
        }
        if ("HOUSEHOLD".equals(type)) company = "";
        result = new Customer(null, name, type, addressArea.getText().trim(), phone, email, company);
        dispose();
    }
}
