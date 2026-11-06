package econest;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class StatCard extends RoundedPanel {

    private final JLabel valueLabel = new JLabel("0", SwingConstants.LEFT);
    private final Color accent;
    private final JLabel hintLabel = new JLabel(" ");

    public StatCard(String caption, Color accent) {
        super(new BorderLayout(0, 2), UITheme.CARD, 16);
        this.accent = accent;

        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel captionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        captionRow.setOpaque(false);
        captionRow.add(new Dot(accent));
        captionRow.add(UITheme.hint(caption.toUpperCase()));

        valueLabel.setFont(UITheme.VALUE);
        valueLabel.setForeground(UITheme.DEEP);

        hintLabel.setFont(UITheme.SMALL);
        hintLabel.setForeground(UITheme.MUTED);

        add(captionRow, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(hintLabel, BorderLayout.SOUTH);
    }

    public void setValue(int value) {
        valueLabel.setText(String.valueOf(value));
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setHint(String hint) {
        hintLabel.setText(hint);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(150, 86);
    }

    private static final class Dot extends JPanel {

        private final Color color;

        private Dot(Color color) {
            this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(9, 9));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 1, 8, 8);
            g2.dispose();
        }
    }
}
