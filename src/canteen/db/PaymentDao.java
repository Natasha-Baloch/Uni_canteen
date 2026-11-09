package canteen.db;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;

public class PaymentDao {
    public static DefaultTableModel table(String statusFilter) throws SQLException {
        return Db.table(
            "SELECT p.payment_id, p.order_id, s.full_name AS customer, p.amount, p.method, p.status, " +
            "IFNULL(DATE_FORMAT(p.paid_at,'%d-%b-%Y %H:%i'),'-') AS paid_at " +
            "FROM payments p JOIN orders o ON o.order_id = p.order_id JOIN students s ON s.student_id = o.student_id " +
            "WHERE (? = 'ALL' OR p.status = ?) ORDER BY p.payment_id DESC", statusFilter, statusFilter);
    }

    /** Owner confirms cash received. */
    public static boolean markPaid(int orderId) throws SQLException {
        return Db.update("UPDATE payments SET status='PAID', paid_at=CURRENT_TIMESTAMP WHERE order_id=? AND status='PENDING'", orderId) > 0;
    }

    /** Customer pays a pending order online (simulated gateway). */
    public static boolean payOnline(int orderId, int studentId) throws SQLException {
        return Db.update("UPDATE payments SET method='ONLINE', status='PAID', paid_at=CURRENT_TIMESTAMP " +
                "WHERE order_id=? AND status='PENDING' AND order_id IN (SELECT order_id FROM orders WHERE student_id=?)",
                orderId, studentId) > 0;
    }
}
