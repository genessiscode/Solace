package econest;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        UITheme.install();

        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
