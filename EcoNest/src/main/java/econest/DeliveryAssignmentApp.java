package econest;

import javax.swing.SwingUtilities;

/** Standalone launcher for the Task 6 module. */
public class DeliveryAssignmentApp {
    public static void main(String[] args) {
        UITheme.install();
        SwingUtilities.invokeLater(() -> new DeliveryFrame().setVisible(true));
    }
}
 
