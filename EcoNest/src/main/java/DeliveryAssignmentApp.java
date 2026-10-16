package econest;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Standalone launcher for the Task 6 module. */
public class DeliveryAssignmentApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new DeliveryFrame().setVisible(true);
        });
    }
}
