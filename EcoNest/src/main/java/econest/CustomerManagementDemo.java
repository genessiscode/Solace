package econest.customer;

import javax.swing.*;

/** Optional standalone preview launcher. */
public class CustomerManagementDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            JFrame frame = new JFrame("EcoNest | Customer Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1180, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new CustomerManagementPanel());
            frame.setVisible(true);
        });
    }
}
