package econest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public final class UITheme {

    public static final Color BG = new Color(0xF2, 0xF8, 0xF3);
    public static final Color CARD = Color.WHITE;
    public static final Color DEEP = new Color(0x1B, 0x43, 0x32);
    public static final Color FOREST = new Color(0x2D, 0x6A, 0x4F);
    public static final Color LEAF = new Color(0x40, 0x91, 0x6C);
    public static final Color MOSS = new Color(0x52, 0xB7, 0x88);
    public static final Color SAGE = new Color(0x95, 0xD5, 0xB2);
    public static final Color MINT = new Color(0xD8, 0xF3, 0xDC);
    public static final Color PALE = new Color(0xEC, 0xF7, 0xEF);
    public static final Color TEXT = new Color(0x1F, 0x35, 0x28);
    public static final Color MUTED = new Color(0x60, 0x74, 0x67);
    public static final Color BORDER = new Color(0xCD, 0xE3, 0xD2);
    public static final Color GRID = new Color(0xE8, 0xF2, 0xEA);
    public static final Color DANGER = new Color(0xA9, 0x40, 0x36);
    public static final Color DANGER_BG = new Color(0xFB, 0xEC, 0xEA);
    public static final Color WARNING = new Color(0xA8, 0x6A, 0x16);
    public static final Color WARNING_BG = new Color(0xFC, 0xF3, 0xDF);
    public static final Color INFO = new Color(0x2B, 0x6B, 0x8A);
    public static final Color INFO_BG = new Color(0xE6, 0xF2, 0xF8);

    public static final Font H1 = new Font("SansSerif", Font.BOLD, 30);
    public static final Font H2 = new Font("SansSerif", Font.BOLD, 18);
    public static final Font H3 = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font SMALL_BOLD = new Font("SansSerif", Font.BOLD, 11);
    public static final Font VALUE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font MONO = new Font("Monospaced", Font.PLAIN, 12);

    private UITheme() {

    }

    public static void install() {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception alsoIgnored) {

            }
        }

        UIManager.put("control", BG);
        UIManager.put("info", PALE);
        UIManager.put("nimbusBase", FOREST);
        UIManager.put("nimbusBlueGrey", BORDER);
        UIManager.put("nimbusLightBackground", CARD);
        UIManager.put("text", TEXT);

        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY_BOLD);
        UIManager.put("Button.disabledText", MUTED);
        UIManager.put("TextField.font", BODY);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.background", CARD);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.background", CARD);
        UIManager.put("Table.font", BODY);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.showGrid", Boolean.FALSE);
        UIManager.put("Table.alternateRowColor", PALE);
        UIManager.put("Table.selectionBackground", MINT);
        UIManager.put("Table.selectionForeground", DEEP);
        UIManager.put("TableHeader.font",
                new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.background", FOREST);
        UIManager.put("ScrollBar.thumb", SAGE);
        UIManager.put("ScrollBar.track", PALE);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("TitledBorder.titleColor", FOREST);
        UIManager.put("TitledBorder.font",
                new Font("SansSerif", Font.BOLD, 13));
        UIManager.put("ToolTip.background", DEEP);
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }

    public static RoundedPanel card() {
        RoundedPanel panel = new RoundedPanel(CARD, 18);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        return panel;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(H2);
        label.setForeground(DEEP);
        return label;
    }

    public static JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_BOLD);
        label.setForeground(FOREST);
        return label;
    }

    public static void styleField(JTextField field) {

        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setCaretColor(FOREST);
        setFieldBorder(field, BORDER);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setFieldBorder(field, MOSS);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setFieldBorder(field, BORDER);
            }
        });
    }

    private static void setFieldBorder(JTextField field, Color color) {
        field.setBorder(new CompoundBorder(
                new RoundedBorder(color, 10, 1),
                new EmptyBorder(7, 10, 7, 10)));
    }

    public static void styleCombo(JComboBox<?> combo) {

        combo.setFont(BODY);
        combo.setForeground(TEXT);
        combo.setBackground(CARD);
        combo.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 10, 1),
                new EmptyBorder(3, 4, 3, 4)));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                label.setFont(BODY);
                label.setBorder(new EmptyBorder(4, 8, 4, 8));
                label.setForeground(selected ? DEEP : TEXT);
                label.setBackground(selected ? MINT : CARD);
                return label;
            }
        });
    }

    public static void styleTable(JTable table) {

        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(CARD);
        table.setSelectionBackground(MINT);
        table.setSelectionForeground(DEEP);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(GRID);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setForeground(Color.WHITE);
        header.setBackground(FOREST);
        header.setPreferredSize(new Dimension(10, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    public static JScrollPane scroll(JComponent content) {

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public static JButton primaryButton(String text) {
        return new RoundedButton(text, FOREST, Color.WHITE, false);
    }

    public static JButton secondaryButton(String text) {
        return new RoundedButton(text, MINT, FOREST, true);
    }

    public static JButton dangerButton(String text) {
        return new RoundedButton(text, DANGER_BG, DANGER, true);
    }

    public static JButton ghostButton(String text) {
        return new RoundedButton(text, PALE, FOREST, false);
    }

    public static JLabel badge(String text, Color background, Color foreground) {

        JLabel label = new JLabel(text);
        label.setFont(SMALL_BOLD);
        label.setForeground(foreground);
        label.setBackground(background);
        label.setOpaque(true);
        label.setBorder(new CompoundBorder(
                new RoundedBorder(background, 12, 0),
                new EmptyBorder(4, 10, 4, 10)));
        return label;
    }

    public static TableCellRenderer statusPillRenderer() {
        return new StatusPillRenderer();
    }

    private static final class HeaderRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);

            label.setOpaque(true);
            label.setBackground(FOREST);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            label.setHorizontalAlignment(LEFT);
            return label;
        }
    }

    private static final class ZebraRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);

            label.setBorder(new EmptyBorder(0, 10, 0, 10));

            if (selected) {
                label.setBackground(MINT);
                label.setForeground(DEEP);
            } else {
                label.setBackground(row % 2 == 0 ? CARD : PALE);
                label.setForeground(TEXT);
            }

            return label;
        }
    }

    private static final class StatusPillRenderer
            implements TableCellRenderer {

        private final JPanelCell cell = new JPanelCell();

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {

            cell.configure(
                    value == null ? "" : value.toString(),
                    selected,
                    row % 2 != 0);
            return cell;
        }
    }

    private static final class JPanelCell extends JLabel {

        private Color pill = MINT;

        private JPanelCell() {
            setOpaque(true);
            setFont(SMALL_BOLD);
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(4, 8, 4, 8));
        }

        private void configure(String value, boolean selected, boolean striped) {

            setText(value);
            setBackground(selected ? MINT : (striped ? PALE : CARD));

            switch (value) {
                case "DELIVERED":
                case "AVAILABLE":
                case "READY":
                    pill = MINT;
                    setForeground(FOREST);
                    break;
                case "ASSIGNED":
                case "PENDING":
                case "CONFIRMED":
                    pill = WARNING_BG;
                    setForeground(WARNING);
                    break;
                case "IN_TRANSIT":
                case "DISPATCHED":
                case "ON_DELIVERY":
                    pill = INFO_BG;
                    setForeground(INFO);
                    break;
                case "FAILED":
                case "CANCELLED":
                case "INACTIVE":
                case "LOW":
                    pill = DANGER_BG;
                    setForeground(DANGER);
                    break;
                default:
                    pill = MINT;
                    setForeground(FOREST);
                    break;
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            super.paintComponent(graphics);

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int textWidth = getFontMetrics(getFont()).stringWidth(getText());
            int width = Math.min(getWidth() - 12, textWidth + 26);
            int height = 22;
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;

            g2.setColor(pill);
            g2.fillRoundRect(x, y, width, height, height, height);

            g2.setColor(getForeground());
            g2.setFont(getFont());
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - g2.getFontMetrics().getHeight()) / 2
                    + g2.getFontMetrics().getAscent();
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }
    }

    public static final class RoundedBorder extends AbstractBorder {

        private final Color color;
        private final int arc;
        private final int thickness;

        public RoundedBorder(Color color, int arc, int thickness) {
            this.color = color;
            this.arc = arc;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics,
                                int x, int y, int width, int height) {

            if (thickness <= 0) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            int inset = thickness;
            g2.drawRoundRect(x + inset / 2, y + inset / 2,
                    width - inset - 1, height - inset - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component component, Insets insets) {
            insets.set(1, 1, 1, 1);
            return insets;
        }
    }
}
