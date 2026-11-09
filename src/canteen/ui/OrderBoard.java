package canteen.ui;

import canteen.db.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Owner's order desk: table of orders + items of the selected order + workflow buttons.
 * requestsOnly = true  -> only PENDING requests with Accept / Reject (used on the dashboard)
 * requestsOnly = false -> all orders with a status filter and every workflow step
 */
public class OrderBoard extends JPanel {
    private static final Map<String, java.util.List<String>> NEXT = new HashMap<>();
    static {
        NEXT.put("PENDING",   Arrays.asList("ACCEPTED", "REJECTED"));
        NEXT.put("ACCEPTED",  Arrays.asList("PREPARING", "REJECTED"));
        NEXT.put("PREPARING", Arrays.asList("READY"));
        NEXT.put("READY",     Arrays.asList("COMPLETED"));
    }

    private final boolean requestsOnly;
    private final JTable table = new JTable();
    private final JTable itemsTable = new JTable();
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"ALL", "PENDING", "ACCEPTED", "PREPARING", "READY", "COMPLETED", "REJECTED", "CANCELLED"});
    private final JLabel alert = Theme.label(" ", Theme.BOLD, Theme.ORANGE);
    private int lastPending = -1;

    public OrderBoard(boolean requestsOnly) {
        this.requestsOnly = requestsOnly;
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        Theme.styleTable(table);
        Theme.styleTable(itemsTable);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showItems(); });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        if (!requestsOnly) {
            JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            f.setOpaque(false);
            f.add(Theme.label("Show:", Theme.BOLD, Theme.TEXT));
            f.add(filter);
            filter.addActionListener(e -> refresh());
            top.add(f, BorderLayout.WEST);
        } else {
            top.add(Theme.label("Incoming order requests", Theme.H2, Theme.TEXT), BorderLayout.WEST);
        }
        top.add(alert, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JScrollPane itemsPane = Theme.scroll(itemsTable);
        itemsPane.setPreferredSize(new Dimension(340, 0));
        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(Theme.scroll(table), BorderLayout.CENTER);
        center.add(itemsPane, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.add(action("Accept", Theme.GREEN, "ACCEPTED"));
        if (!requestsOnly) {
            btns.add(action("Start Preparing", Theme.PURPLE, "PREPARING"));
            btns.add(action("Mark Ready", Theme.BLUE, "READY"));
            btns.add(action("Complete", new Color(0x1B7A45), "COMPLETED"));
        }
        btns.add(action("Reject", Theme.RED, "REJECTED"));
        if (!requestsOnly) {
            Theme.RoundButton cash = Theme.button("Cash Received", Theme.ORANGE);
            cash.addActionListener(e -> cashReceived());
            btns.add(cash);
        }
        Theme.RoundButton ref = Theme.button("Refresh", Theme.NAVY);
        ref.addActionListener(e -> refresh());
        btns.add(ref);
        add(btns, BorderLayout.SOUTH);

        new javax.swing.Timer(5000, e -> { if (isShowing()) refresh(); }).start();
    }

    private Theme.RoundButton action(String text, Color c, String status) {
        Theme.RoundButton b = Theme.button(text, c);
        b.addActionListener(e -> setStatus(status));
        return b;
    }

    public void refresh() {
        try {
            int keep = selectedId();
            String f = requestsOnly ? "PENDING" : String.valueOf(filter.getSelectedItem());
            DefaultTableModel m = OrderDao.ownerOrders(f);
            table.setModel(m);
            for (int r = 0; r < m.getRowCount(); r++)
                if (((Number) m.getValueAt(r, 0)).intValue() == keep) { table.setRowSelectionInterval(r, r); break; }
            if (table.getSelectedRow() < 0) itemsTable.setModel(new DefaultTableModel());
            // new-request alert
            int pending = (int) ReportDao.pendingOrders();
            if (lastPending >= 0 && pending > lastPending) {
                Toolkit.getDefaultToolkit().beep();
                alert.setText("New order request received!");
            } else if (pending == 0) alert.setText(" ");
            lastPending = pending;
        } catch (SQLException e) { /* ignore silent refresh errors */ }
    }

    private int selectedId() {
        int r = table.getSelectedRow();
        return r < 0 ? -1 : ((Number) table.getValueAt(r, 0)).intValue();
    }

    private String cell(String col) {
        int r = table.getSelectedRow();
        int c = ((javax.swing.table.AbstractTableModel) table.getModel()).findColumn(col);
        return (r < 0 || c < 0) ? null : String.valueOf(table.getModel().getValueAt(r, c));
    }

    private void showItems() {
        int id = selectedId();
        if (id < 0) return;
        try { itemsTable.setModel(OrderDao.items(id)); } catch (SQLException e) { warn(Db.friendly(e)); }
    }

    private void setStatus(String next) {
        int id = selectedId();
        if (id < 0) { warn("Select an order first."); return; }
        String cur = cell("STATUS");
        if (!NEXT.getOrDefault(cur, Collections.emptyList()).contains(next)) {
            warn("Order #" + id + " is " + cur + ". It cannot move to " + next + ".\nWorkflow: PENDING > ACCEPTED > PREPARING > READY > COMPLETED");
            return;
        }
        try {
            if (next.equals("COMPLETED") && "PENDING".equals(cell("PAY STATUS"))) {
                int a = JOptionPane.showConfirmDialog(this, "Payment is still pending.\nHave you received Rs " + cell("TOTAL") + " in cash?",
                        "Collect payment", JOptionPane.YES_NO_OPTION);
                if (a != JOptionPane.YES_OPTION) return;
                PaymentDao.markPaid(id);
            }
            if (next.equals("REJECTED") && JOptionPane.showConfirmDialog(this, "Reject order #" + id + "?" +
                    ("PAID".equals(cell("PAY STATUS")) ? "\nThe online payment will be marked REFUNDED." : ""),
                    "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            OrderDao.updateStatus(id, next);
            refresh();
        } catch (SQLException e) { warn(Db.friendly(e)); }
    }

    private void cashReceived() {
        int id = selectedId();
        if (id < 0) { warn("Select an order first."); return; }
        try {
            if (PaymentDao.markPaid(id)) { JOptionPane.showMessageDialog(this, "Payment for order #" + id + " marked as PAID."); refresh(); }
            else warn("This order has no pending payment.");
        } catch (SQLException e) { warn(Db.friendly(e)); }
    }

    private void warn(String m) { JOptionPane.showMessageDialog(this, m, "Canteen", JOptionPane.WARNING_MESSAGE); }
}
