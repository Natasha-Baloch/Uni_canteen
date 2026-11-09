package canteen.ui;

import canteen.model.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Main window for the CUSTOMER role: Menu (browse + cart) and My Orders (live tracking). */
public class CustomerFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final MenuPanel menu;
    private final MyOrdersPanel orders = new MyOrdersPanel();
    private Sidebar side;
    private final JLabel title = Theme.label("Menu", Theme.H1, Theme.TEXT);

    public CustomerFrame() {
        setTitle("UniCanteen - Customer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1340, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        menu = new MenuPanel(() -> side.select(1));
        content.add(menu, "menu");
        content.add(orders, "orders");

        side = new Sidebar(Theme.YELLOW_LIGHT, Theme.YELLOW, Theme.BROWN, Theme.BROWN,
                "UniCanteen.", Theme.YELLOW, Theme.BROWN);
        side.addItem("Menu", () -> go(0));
        side.addItem("My Orders", () -> go(1));
        side.addBottom("Logout", this::logout);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));
        header.add(title, BorderLayout.WEST);
        JLabel who = Theme.label("Hello, " + Session.fullName, Theme.BOLD, Theme.BROWN);
        who.setOpaque(true);
        who.setBackground(Theme.YELLOW_LIGHT);
        who.setBorder(new EmptyBorder(8, 16, 8, 16));
        header.add(who, BorderLayout.EAST);

        add(side, BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        side.select(0);
    }

    private void go(int i) {
        if (i == 0) { title.setText("Menu"); cards.show(content, "menu"); }
        else { title.setText("My Orders"); orders.refresh(); cards.show(content, "orders"); }
    }

    private void logout() {
        orders.stop();
        Session.clear();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
