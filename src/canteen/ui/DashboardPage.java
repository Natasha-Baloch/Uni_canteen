package canteen.ui;

import canteen.db.ReportDao;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.sql.SQLException;

/** Owner dashboard: 5 statistic cards + live incoming order requests. */
public class DashboardPage extends BasePage {
    private final JLabel students = valueLabel(), foods = valueLabel(), orders = valueLabel(),
            sales = valueLabel(), pending = valueLabel();
    private final OrderBoard board = new OrderBoard(true);

    public DashboardPage() {
        JPanel cards = new JPanel(new GridLayout(1, 5, 14, 0));
        cards.setOpaque(false);
        cards.add(statCard("Total Students", students, Theme.BLUE));
        cards.add(statCard("Food Items", foods, Theme.PURPLE));
        cards.add(statCard("Total Orders", orders, Theme.ORANGE));
        cards.add(statCard("Total Sales (paid)", sales, Theme.GREEN));
        cards.add(statCard("Pending Orders", pending, Theme.RED));
        cards.setPreferredSize(new Dimension(0, 100));
        add(cards, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        new Timer(5000, e -> { if (isShowing()) loadStats(); }).start();
    }

    private static JLabel valueLabel() { return Theme.label("0", new Font("Segoe UI", Font.BOLD, 26), Theme.TEXT); }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel c = Theme.card();
        c.setLayout(new BorderLayout());
        c.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 6, 0, 0, accent),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        c.add(Theme.label(title, Theme.BOLD, Theme.MUTED), BorderLayout.NORTH);
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    private void loadStats() {
        try {
            students.setText(String.valueOf(ReportDao.count("students")));
            foods.setText(String.valueOf(ReportDao.count("food_items")));
            orders.setText(String.valueOf(ReportDao.count("orders")));
            sales.setText("Rs " + String.format("%,.0f", ReportDao.totalSales()));
            pending.setText(String.valueOf(ReportDao.pendingOrders()));
        } catch (SQLException e) { error(e); }
    }

    @Override public void refresh() { loadStats(); board.refresh(); }
}
