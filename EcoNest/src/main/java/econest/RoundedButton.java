package econest;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {

    private final Color base;
    private final boolean outlined;
    private boolean hovered;

    public RoundedButton(String text, Color base, Color foreground,
                         boolean outlined) {

        super(text);
        this.base = base;
        this.outlined = outlined;

        setFont(UITheme.BODY_BOLD);
        setForeground(foreground);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(9, 18, 9, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        boolean pressed = getModel().isPressed();

        Color fill;
        if (!isEnabled()) {
            fill = UITheme.PALE;
        } else if (outlined) {
            fill = hovered ? base : UITheme.CARD;
        } else if (pressed) {
            fill = base.darker().darker();
        } else if (hovered) {
            fill = base.darker();
        } else {
            fill = base;
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, width - 1, height - 1, height, height);

        if (outlined && isEnabled()) {
            g2.setColor(base.darker());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, height, height);
        }

        if (hasFocus() && isEnabled()) {
            g2.setColor(UITheme.MOSS);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, width - 5, height - 5, height, height);
        }

        g2.dispose();
        super.paintComponent(graphics);
    }
}
