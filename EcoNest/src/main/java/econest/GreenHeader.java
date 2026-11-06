package econest;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GreenHeader extends JPanel {

    private static final BufferedImage LEAF_LARGE =
            loadLeaf("leaf-large.png");
    private static final BufferedImage LEAF_MEDIUM =
            loadLeaf("leaf-medium.png");
    private static final BufferedImage LEAF_SMALL =
            loadLeaf("leaf-small.png");

    private final String title;
    private final String subtitle;

    public GreenHeader(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setOpaque(false);
        setPreferredSize(new Dimension(10, 116));
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        g2.setPaint(new GradientPaint(
                0, 0, UITheme.DEEP,
                width, height, UITheme.FOREST));
        g2.fillRoundRect(0, 0, width - 1, height - 1, 24, 24);

        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillOval(width - 210, -90, 300, 300);
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillOval(width - 150, 55, 190, 190);

        drawLeaf(g2, LEAF_LARGE, width - 232, 26, 156, 88);
        drawLeaf(g2, LEAF_MEDIUM, width - 150, 46, 100, 53);
        drawLeaf(g2, LEAF_SMALL, width - 92, 74, 62, 28);

        g2.setColor(Color.WHITE);
        g2.setFont(UITheme.H1);
        g2.drawString(title, 28, 54);

        g2.setColor(UITheme.MINT);
        g2.setFont(UITheme.BODY);
        g2.drawString(subtitle, 28, 82);

        String brand = "GREEN BY DESIGN";
        g2.setFont(UITheme.SMALL_BOLD);
        FontMetrics metrics = g2.getFontMetrics();
        int brandWidth = metrics.stringWidth(brand) + 24;
        int brandX = width - brandWidth - 28;
        g2.setColor(new Color(255, 255, 255, 38));
        g2.fillRoundRect(brandX, 22, brandWidth, 24, 12, 12);
        g2.setColor(new Color(255, 255, 255, 210));
        g2.drawString(brand, brandX + 12,
                22 + (24 - metrics.getHeight()) / 2 + metrics.getAscent());

        g2.setColor(new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 24, 24);

        g2.dispose();
        super.paintComponent(graphics);
    }

    private static void drawLeaf(Graphics2D g2, BufferedImage image,
                                 int x, int y, int width, int height) {

        if (image != null) {
            g2.drawImage(image, x, y, width, height, null);
        }
    }

    private static BufferedImage loadLeaf(String name) {

        String path = "/assets/leaves/" + name;

        try (InputStream stream =
                     GreenHeader.class.getResourceAsStream(path)) {

            return stream == null ? null : ImageIO.read(stream);
        } catch (IOException ignored) {
            return null;
        }
    }
}
