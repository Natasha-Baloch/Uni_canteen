package canteen.ui;

import canteen.db.ReportDao;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Owner: sales reports built with JOIN, GROUP BY, HAVING and subqueries. */
public class ReportsPage extends BasePage {
    private final JTable table = new JTable();
    private final JComboBox<String> box = new JComboBox<>(ReportDao.REPORT_NAMES);
    private final JLabel caption = Theme.label(" ", Theme.BASE, Theme.MUTED);

    public ReportsPage() {
        Theme.styleTable(table);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(Theme.label("Report:", Theme.BOLD, Theme.TEXT));
        top.add(box);
        Theme.RoundButton run = Theme.button("Generate", Theme.YELLOW, Theme.BROWN);
        run.addActionListener(e -> refresh());
        box.addActionListener(e -> refresh());
        top.add(run);
        top.add(caption);
        add(top, BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
    }

    @Override public void refresh() {
        try {
            table.setModel(ReportDao.run(box.getSelectedIndex()));
            caption.setText("  " + table.getRowCount() + " row(s)");
        } catch (SQLException e) { error(e); }
    }
}
