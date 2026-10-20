import frontend.DeliveryAgentForm;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DeliveryAgentForm().setVisible(true);
        });
    }
}
