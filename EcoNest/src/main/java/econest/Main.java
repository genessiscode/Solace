package econest;

import econest.ui.OrderFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderFrame().setVisible(true));
    }
}
