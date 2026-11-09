package canteen.ui;

import canteen.db.PaymentDao;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Owner: payment history and recording cash payments. */
public class PaymentsPage extends BasePage {
    private final JTable table = new JTable();
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"ALL", "PENDING", "PAID", "FAILED", "REFUNDED"});

    public PaymentsPage() {
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(Theme.label("Show:", Theme.BOLD, Theme.TEXT));
        top.add(filter);
        filter.addActionListener(e -> refresh());
        Theme.RoundButton paid = Theme.button("Record Cash Payment (mark PAID)", Theme.GREEN);
        paid.addActionListener(e -> markPaid());
        Theme.RoundButton ref = Theme.button("Refresh", Theme.NAVY);
        ref.addActionListener(e -> refresh());
        top.add(paid);
        top.add(ref);
        add(top, BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
    }

    @Override public void refresh() {
        try { table.setModel(PaymentDao.table(String.valueOf(filter.getSelectedItem()))); } catch (SQLException e) { error(e); }
    }

    private void markPaid() {
        int r = table.getSelectedRow();
        if (r < 0) { info("Select a payment first."); return; }
        int orderId = ((Number) table.getValueAt(r, 1)).intValue();
        try {
            if (PaymentDao.markPaid(orderId)) { info("Payment for order #" + orderId + " recorded as PAID."); refresh(); }
            else info("Only PENDING payments can be marked as paid.");
        } catch (SQLException e) { error(e); }
    }
}
