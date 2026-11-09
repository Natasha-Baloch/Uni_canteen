package canteen.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/** Colours, fonts and small helper builders so every screen looks consistent. */
public class Theme {
    public static final Color YELLOW = new Color(0xFFB800);
    public static final Color YELLOW_LIGHT = new Color(0xFFF5D6);
    public static final Color BROWN = new Color(0x3E160C);
    public static final Color NAVY = new Color(0x1F4A7A);
    public static final Color SKY = new Color(0xA5CFF1);
    public static final Color BG = new Color(0xF6F7F9);
    public static final Color TEXT = new Color(0x222222);
    public static final Color MUTED = new Color(0x777777);
    public static final Color GREEN = new Color(0x2E9E5B);
    public static final Color RED = new Color(0xE0533D);
    public static final Color BLUE = new Color(0x3A7BD5);
    public static final Color ORANGE = new Color(0xF2994A);
    public static final Color PURPLE = new Color(0x8E44AD);
    public static final Color BORDER = new Color(0xE3E6EA);

    public static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 28);

    // ---------- rounded button (looks the same on every Look & Feel) ----------
    public static class RoundButton extends JButton {
        private Color bg;
        private boolean hover;

        public RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            setForeground(fg);
            setFont(BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(9, 16, 9, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public void setColor(Color c) { bg = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xD0D0D0)
                    : getModel().isPressed() ? shade(bg, -35) : hover ? shade(bg, -18) : bg;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Color shade(Color c, int d) {
        return new Color(Math.max(0, Math.min(255, c.getRed() + d)),
                         Math.max(0, Math.min(255, c.getGreen() + d)),
                         Math.max(0, Math.min(255, c.getBlue() + d)));
    }

    public static RoundButton button(String text, Color bg, Color fg) { return new RoundButton(text, bg, fg); }
    public static RoundButton button(String text, Color bg) { return new RoundButton(text, bg, Color.WHITE); }

    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    public static JTextField field(int cols) {
        JTextField t = new JTextField(cols);
        t.setFont(BASE);
        t.setBorder(new CompoundBorder(new LineBorder(new Color(0xC9CED6), 1, true), new EmptyBorder(7, 10, 7, 10)));
        return t;
    }

    public static JPasswordField password(int cols) {
        JPasswordField t = new JPasswordField(cols);
        t.setFont(BASE);
        t.setBorder(new CompoundBorder(new LineBorder(new Color(0xC9CED6), 1, true), new EmptyBorder(7, 10, 7, 10)));
        return t;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    /** One label + component row in a GridBagLayout form. */
    public static void formRow(JPanel p, int row, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(5, 0, 5, 10);
        JLabel l = new JLabel(label);
        l.setFont(BOLD);
        p.add(l, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(5, 0, 5, 0);
        p.add(c, g);
    }

    public static Color statusColor(String s) {
        if (s == null) return TEXT;
        switch (s) {
            case "PENDING":   return ORANGE;
            case "ACCEPTED":  return BLUE;
            case "PREPARING": return PURPLE;
            case "READY":     return GREEN;
            case "COMPLETED": return new Color(0x1B7A45);
            case "PAID":      return GREEN;
            case "REFUNDED":  return new Color(0x6C7A89);
            case "REJECTED": case "CANCELLED": case "FAILED": return RED;
            default: return TEXT;
        }
    }

    /** Zebra rows, blue header, coloured STATUS cells. */
    public static void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setFont(BASE);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xCFE4F7));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        DefaultTableCellRenderer body = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, foc, r, c);
                setHorizontalAlignment(LEFT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF3F8FC));
                String col = tb.getColumnName(c);
                if (col.equals("STATUS") || col.equals("PAY STATUS")) {
                    setForeground(statusColor(String.valueOf(v)));
                    setFont(BOLD);
                } else {
                    setForeground(TEXT);
                    setFont(BASE);
                }
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, body);
        t.setDefaultRenderer(Number.class, body);
        DefaultTableCellRenderer head = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, foc, r, c);
                setBackground(SKY);
                setForeground(new Color(0x1C2B3A));
                setFont(BOLD);
                setHorizontalAlignment(LEFT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        t.getTableHeader().setDefaultRenderer(head);
        t.getTableHeader().setPreferredSize(new Dimension(100, 34));
        t.getTableHeader().setReorderingAllowed(false);
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }
}
