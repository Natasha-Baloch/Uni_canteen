package canteen.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Left navigation bar used by both the customer and the owner window. */
public class Sidebar extends JPanel {
    private final JPanel top = new JPanel();
    private final JPanel bottom = new JPanel();
    private final List<Theme.RoundButton> items = new ArrayList<>();
    private final Color bg, selBg, selFg, fg;

    public Sidebar(Color bg, Color selBg, Color selFg, Color fg, String brand, Color brandBg, Color brandFg) {
        this.bg = bg; this.selBg = selBg; this.selFg = selFg; this.fg = fg;
        setLayout(new BorderLayout());
        setBackground(bg);
        setPreferredSize(new Dimension(220, 0));

        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(brandBg);
        brandPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        brandPanel.add(Theme.label(brand, new Font("Segoe UI", Font.BOLD, 22), brandFg), BorderLayout.WEST);

        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(18, 12, 0, 12));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 12, 18, 12));

        add(brandPanel, BorderLayout.NORTH);
        add(top, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private Theme.RoundButton make(String text) {
        Theme.RoundButton b = new Theme.RoundButton(text, bg, fg);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setBorder(new EmptyBorder(11, 16, 11, 16));
        return b;
    }

    public JButton addItem(String text, Runnable action) {
        Theme.RoundButton b = make(text);
        b.addActionListener(e -> { highlight(b); action.run(); });
        items.add(b);
        top.add(b);
        top.add(Box.createVerticalStrut(6));
        return b;
    }

    public void addBottom(String text, Runnable action) {
        Theme.RoundButton b = make(text);
        b.addActionListener(e -> action.run());
        bottom.add(b);
    }

    private void highlight(Theme.RoundButton sel) {
        for (Theme.RoundButton b : items) {
            b.setColor(b == sel ? selBg : bg);
            b.setForeground(b == sel ? selFg : fg);
        }
    }

    public void select(int i) { items.get(i).doClick(); }
}
