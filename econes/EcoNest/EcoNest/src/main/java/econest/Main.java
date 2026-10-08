package econest;

import javax.swing.*;

/**
 * Application entry point. Launches the EcoNest dashboard.
 *
 * <p>Run with {@code mvn exec:java}, or from an IDE by running this class.</p>
 */
public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
