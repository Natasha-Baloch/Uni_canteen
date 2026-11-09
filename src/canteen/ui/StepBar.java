package canteen.ui;

import javax.swing.*;
import java.awt.*;

/** Order progress tracker:  Placed - Accepted - Preparing - Ready - Completed */
public class StepBar extends JPanel {
    private static final String[] KEYS = {"PENDING", "ACCEPTED", "PREPARING", "READY", "COMPLETED"};
    private static final String[] NAMES = {"Placed", "Accepted", "Preparing", "Ready", "Completed"};
    private String status = "";

    public StepBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(500, 70));
    }

    public void setStatus(String s) { status = s == null ? "" : s; repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), n = KEYS.length, cy = 22, r = 12;
        boolean dead = status.equals("REJECTED") || status.equals("CANCELLED");
        int idx = -1;
        for (int i = 0; i < n; i++) if (KEYS[i].equals(status)) idx = i;
        if (status.isEmpty()) { g.dispose(); return; }
        if (dead) {
            g.setFont(Theme.H2);
            g.setColor(Theme.RED);
            String t = "This order was " + status.toLowerCase() + ".";
            g.drawString(t, (w - g.getFontMetrics().stringWidth(t)) / 2, 38);
            g.dispose();
            return;
        }
        for (int i = 0; i < n; i++) {
            int x = w / (2 * n) + i * w / n;
            if (i < n - 1) {
                int x2 = w / (2 * n) + (i + 1) * w / n;
                g.setColor(i < idx ? Theme.GREEN : new Color(0xD5D9DE));
                g.setStroke(new BasicStroke(4f));
                g.drawLine(x + r, cy, x2 - r, cy);
            }
            boolean done = i <= idx;
            g.setColor(done ? Theme.GREEN : new Color(0xD5D9DE));
            g.fillOval(x - r, cy - r, 2 * r, 2 * r);
            g.setColor(Color.WHITE);
            g.setFont(Theme.BOLD);
            String m = done ? "\u2713" : String.valueOf(i + 1);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(m, x - fm.stringWidth(m) / 2, cy + fm.getAscent() / 2 - 1);
            g.setColor(i == idx ? Theme.TEXT : Theme.MUTED);
            g.setFont(i == idx ? Theme.BOLD : Theme.SMALL);
            fm = g.getFontMetrics();
            g.drawString(NAMES[i], x - fm.stringWidth(NAMES[i]) / 2, cy + r + 20);
        }
        g.dispose();
    }
}
