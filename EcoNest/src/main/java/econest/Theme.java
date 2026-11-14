package econest.customer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

final class Theme {
    static final Color DARK = new Color(31, 75, 57);
    static final Color GREEN = new Color(63, 125, 88);
    static final Color MINT = new Color(226, 240, 229);
    static final Color BACKGROUND = new Color(246, 249, 245);
    static final Color WHITE = Color.WHITE;
    static final Color TEXT = new Color(49, 63, 54);
    static final Color MUTED = new Color(119, 135, 124);
    static final Color BORDER = new Color(222, 231, 222);
    static final Color RED = new Color(183, 76, 66);

    private Theme() {}

    static Font headingFont(int size) { return new Font("SansSerif", Font.BOLD, size); }
    static Font bodyFont(int style, int size) { return new Font("SansSerif", style, size); }

    static Border inputBorder() {
        return new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10));
    }

    static JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(bodyFont(Font.BOLD, 12));
        b.setForeground(primary ? WHITE : DARK);
        b.setBackground(primary ? GREEN : WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(primary ? GREEN : BORDER, 1, true),
                                       new EmptyBorder(9, 15, 9, 15)));
        b.setOpaque(true);
        return b;
    }
}
