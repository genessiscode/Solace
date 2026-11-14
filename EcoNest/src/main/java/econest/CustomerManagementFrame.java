package econest;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * Window wrapper that connects the Client Management module to the EcoNest
 * dashboard. The actual customer CRUD interface is CustomerManagementPanel.
 */
public final class CustomerManagementFrame extends JFrame {

    public CustomerManagementFrame() {
        setTitle("EcoNest | Client Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setContentPane(new CustomerManagementPanel());
    }
}
