package canteen.ui;

import canteen.db.*;
import canteen.model.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Customer order history with live status tracking (auto-refreshes every 4 seconds). */
public class MyOrdersPanel extends JPanel {
    private final JTable table = new JTable();
    private final JTable itemsTable = new JTable();
    private final StepBar steps = new StepBar();
    private final Map<Integer, String> lastStatus = new HashMap<>();
    private final Timer timer;
    private boolean firstLoad = true;

    public MyOrdersPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        Theme.styleTable(table);
        Theme.styleTable(itemsTable);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showDetails(); });

        JPanel stepCard = Theme.card();
        stepCard.setLayout(new BorderLayout());
        stepCard.add(Theme.label("Order progress (select an order)", Theme.BOLD, Theme.MUTED), BorderLayout.NORTH);
        stepCard.add(steps, BorderLayout.CENTER);

        JScrollPane itemsPane = Theme.scroll(itemsTable);
        itemsPane.setPreferredSize(new Dimension(300, 0));
        JPanel lower = new JPanel(new BorderLayout(12, 0));
        lower.setOpaque(false);
        lower.add(stepCard, BorderLayout.CENTER);
        lower.add(itemsPane, BorderLayout.EAST);
        lower.setPreferredSize(new Dimension(0, 160));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        Theme.RoundButton cancel = Theme.button("Cancel Order", Theme.RED);
        Theme.RoundButton pay = Theme.button("Pay Online Now", Theme.GREEN);
        Theme.RoundButton refresh = Theme.button("Refresh", Theme.NAVY);
        cancel.addActionListener(e -> cancelOrder());
        pay.addActionListener(e -> payOnline());
        refresh.addActionListener(e -> refresh());
        buttons.add(cancel);
        buttons.add(pay);
        buttons.add(refresh);

        add(buttons, BorderLayout.NORTH);
        add(Theme.scroll(table), BorderLayout.CENTER);
        add(lower, BorderLayout.SOUTH);

        timer = new Timer(4000, e -> { if (isShowing()) load(true); });
        timer.start();
    }

    public void stop() { timer.stop(); }

    public void refresh() { load(false); }

    private int selectedOrderId() {
        int r = table.getSelectedRow();
        return r < 0 ? -1 : ((Number) table.getValueAt(r, 0)).intValue();
    }

    private String selectedCell(String col) {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        int c = ((javax.swing.table.AbstractTableModel) table.getModel()).findColumn(col);
        return c < 0 ? null : String.valueOf(table.getModel().getValueAt(r, c));
    }

    private void load(boolean notify) {
        try {
            int keep = selectedOrderId();
            DefaultTableModel m = OrderDao.customerOrders(Session.studentId);
            // look for status changes made by the owner
            for (int r = 0; r < m.getRowCount(); r++) {
                int id = ((Number) m.getValueAt(r, 0)).intValue();
                String st = String.valueOf(m.getValueAt(r, m.findColumn("STATUS")));
                String old = lastStatus.put(id, st);
                if (notify && !firstLoad && old != null && !old.equals(st)) announce(id, st);
            }
            firstLoad = false;
            table.setModel(m);
            for (int r = 0; r < m.getRowCount(); r++)
                if (((Number) m.getValueAt(r, 0)).intValue() == keep) { table.setRowSelectionInterval(r, r); break; }
            if (table.getSelectedRow() < 0) { itemsTable.setModel(new DefaultTableModel()); steps.setStatus(""); }
        } catch (SQLException e) {
            if (!notify) JOptionPane.showMessageDialog(this, Db.friendly(e), "Problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void announce(int id, String st) {
        String msg;
        switch (st) {
            case "ACCEPTED":  msg = "The canteen accepted your order #" + id + "."; break;
            case "PREPARING": msg = "Order #" + id + " is being prepared."; break;
            case "READY":     msg = "Order #" + id + " is READY - please collect it at the counter!"; break;
            case "COMPLETED": msg = "Order #" + id + " is completed. Enjoy your meal!"; break;
            case "REJECTED":  msg = "Sorry, order #" + id + " was rejected by the canteen."; break;
            default: return;
        }
        Toolkit.getDefaultToolkit().beep();
        final String m = msg;
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, m, "Order update", JOptionPane.INFORMATION_MESSAGE));
    }

    private void showDetails() {
        int id = selectedOrderId();
        if (id < 0) return;
        try {
            itemsTable.setModel(OrderDao.items(id));
            steps.setStatus(selectedCell("STATUS"));
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, Db.friendly(e)); }
    }

    private void cancelOrder() {
        int id = selectedOrderId();
        if (id < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Cancel order #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            if (OrderDao.cancelByCustomer(id, Session.studentId)) { JOptionPane.showMessageDialog(this, "Order cancelled."); lastStatus.put(id, "CANCELLED"); }
            else JOptionPane.showMessageDialog(this, "Only orders that are still PENDING can be cancelled.\nThe canteen has already started on it.");
            load(false);
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, Db.friendly(e)); }
    }

    private void payOnline() {
        int id = selectedOrderId();
        if (id < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
        if (!"PENDING".equals(selectedCell("PAY STATUS"))) { JOptionPane.showMessageDialog(this, "This order has no pending payment."); return; }
        if (JOptionPane.showConfirmDialog(this, "Pay order #" + id + " online now (simulated gateway)?", "Online payment", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            if (PaymentDao.payOnline(id, Session.studentId)) JOptionPane.showMessageDialog(this, "Payment successful. Thank you!");
            load(false);
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, Db.friendly(e)); }
    }
}
