package canteen.ui;

import canteen.model.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main window for the OWNER role (navy sidebar + page header like a restaurant admin panel). */
public class OwnerFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, BasePage> pages = new LinkedHashMap<>();
    private final JLabel path = Theme.label("/Home/Dashboard", Theme.SMALL, new Color(0x2C4A68));
    private final JLabel title = Theme.label("DASHBOARD", new Font("Serif", Font.BOLD, 36), new Color(0x142A40));

    public OwnerFrame() {
        setTitle("UniCanteen - Owner Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 820);
        setMinimumSize(new Dimension(1150, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        pages.put("Dashboard", new DashboardPage());
        pages.put("Orders", new OrdersPage());
        pages.put("Menu", new FoodPage());
        pages.put("Categories", new CategoriesPage());
        pages.put("Students", new StudentsPage());
        pages.put("Payments", new PaymentsPage());
        pages.put("Reports", new ReportsPage());
        for (Map.Entry<String, BasePage> e : pages.entrySet()) content.add(e.getValue(), e.getKey());

        Sidebar side = new Sidebar(Theme.NAVY, new Color(0x3C7AB8), Color.WHITE, Color.WHITE,
                "UniCanteen", new Color(0x173A60), Color.WHITE);
        for (String n : pages.keySet()) side.addItem(n, () -> show(n));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SKY);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(path);
        t.add(title);
        header.add(t, BorderLayout.WEST);

        JPanel ownerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        ownerActions.setOpaque(false);
        ownerActions.add(Theme.label("Owner: " + Session.fullName, Theme.BOLD, new Color(0x142A40)));
        Theme.RoundButton logout = Theme.button("Logout", Theme.RED, Color.WHITE);
        logout.addActionListener(e -> logout());
        ownerActions.add(logout);
        header.add(ownerActions, BorderLayout.EAST);

        JPanel main = new JPanel(new BorderLayout());
        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        add(side, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);
        side.select(0);
    }

    private void show(String name) {
        path.setText("/Home/" + name);
        title.setText(name.toUpperCase());
        pages.get(name).refresh();
        cards.show(content, name);
    }

    private void logout() {
        Session.clear();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
