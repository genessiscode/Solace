package econest;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {

    private final Color fill;
    private final int arc;
    private Color borderColor;
    private boolean shadow = true;

    public RoundedPanel(Color fill, int arc) {
        this(null, fill, arc);
    }

    public RoundedPanel(LayoutManager layout, Color fill, int arc) {
        super(layout);
        this.fill = fill;
        this.arc = arc;
        setOpaque(false);
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (shadow) {
            g2.setColor(new Color(23, 52, 35, 18));
            g2.fillRoundRect(2, 4, width - 4, height - 5, arc, arc);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
        }

        g2.dispose();
        super.paintComponent(graphics);
    }
}
