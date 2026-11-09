package canteen.db;

import canteen.model.CartItem;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.List;

/** Everything about orders: placing, status workflow, history. */
public class OrderDao {

    /**
     * Places a new order (status PENDING) and its payment row in ONE transaction.
     * Prices are re-read from FOOD_ITEMS so the customer can never fake a price.
     * ONLINE payments are marked PAID immediately (simulated gateway); CASH stays PENDING.
     */
    public static int placeOrder(int studentId, List<CartItem> items, String method, String notes) throws SQLException {
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orders (student_id, status, total_amount, notes) VALUES (?, 'PENDING', 0, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, notes == null || notes.isEmpty() ? null : notes);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); orderId = rs.getInt(1); }
                }
                double total = 0;
                for (CartItem it : items) {
                    double price; String avail;
                    try (PreparedStatement ps = c.prepareStatement("SELECT price, availability FROM food_items WHERE food_id = ?")) {
                        ps.setInt(1, it.food.id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException(it.food.name + " no longer exists.");
                            price = rs.getDouble(1); avail = rs.getString(2);
                        }
                    }
                    if (!"Y".equals(avail.trim())) throw new SQLException(it.food.name + " is sold out. Please remove it from your cart.");
                    double line = price * it.qty;
                    total += line;
                    Db.update(c, "INSERT INTO order_details (order_id, food_id, quantity, unit_price, line_total) VALUES (?,?,?,?,?)",
                            orderId, it.food.id, it.qty, price, line);
                }
                Db.update(c, "UPDATE orders SET total_amount = ? WHERE order_id = ?", total, orderId);
                if ("ONLINE".equals(method))
                    Db.update(c, "INSERT INTO payments (order_id, amount, method, status, paid_at) VALUES (?,?,?,'PAID',CURRENT_TIMESTAMP)", orderId, total, method);
                else
                    Db.update(c, "INSERT INTO payments (order_id, amount, method, status) VALUES (?,?,?,'PENDING')", orderId, total, method);
                c.commit();
                return orderId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Owner: change the order status. Rejecting/cancelling also settles the payment (refund / failed). */
    public static void updateStatus(int orderId, String status) throws SQLException {
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                Db.update(c, "UPDATE orders SET status = ? WHERE order_id = ?", status, orderId);
                if (status.equals("REJECTED") || status.equals("CANCELLED"))
                    Db.update(c, "UPDATE payments SET status = CASE status WHEN 'PAID' THEN 'REFUNDED' WHEN 'PENDING' THEN 'FAILED' ELSE status END WHERE order_id = ?", orderId);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Customer: cancel own order, only while it is still PENDING. Returns false if not allowed. */
    public static boolean cancelByCustomer(int orderId, int studentId) throws SQLException {
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                int n = Db.update(c, "UPDATE orders SET status='CANCELLED' WHERE order_id=? AND student_id=? AND status='PENDING'", orderId, studentId);
                if (n == 0) { c.rollback(); return false; }
                Db.update(c, "UPDATE payments SET status = CASE status WHEN 'PAID' THEN 'REFUNDED' WHEN 'PENDING' THEN 'FAILED' ELSE status END WHERE order_id = ?", orderId);
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Owner view. statusFilter = ALL or one status. */
    public static DefaultTableModel ownerOrders(String statusFilter) throws SQLException {
        return Db.table(
            "SELECT o.order_id, s.full_name AS customer, DATE_FORMAT(o.order_date,'%d-%b %H:%i') AS ordered_at, " +
            "o.total_amount AS total, o.status, p.method AS pay_method, p.status AS pay_status, IFNULL(o.notes,'-') AS notes " +
            "FROM orders o JOIN students s ON s.student_id = o.student_id LEFT JOIN payments p ON p.order_id = o.order_id " +
            "WHERE (? = 'ALL' OR o.status = ?) ORDER BY o.order_id DESC", statusFilter, statusFilter);
    }

    /** Customer view: own order history, with the items listed using a correlated subquery + GROUP_CONCAT. */
    public static DefaultTableModel customerOrders(int studentId) throws SQLException {
        return Db.table(
            "SELECT o.order_id, DATE_FORMAT(o.order_date,'%d-%b %H:%i') AS ordered_at, " +
            "(SELECT GROUP_CONCAT(CONCAT(f.food_name, ' x', d.quantity) ORDER BY f.food_name SEPARATOR ', ') " +
            "   FROM order_details d JOIN food_items f ON f.food_id = d.food_id WHERE d.order_id = o.order_id) AS items, " +
            "o.total_amount AS total, o.status, p.method AS pay_method, p.status AS pay_status " +
            "FROM orders o LEFT JOIN payments p ON p.order_id = o.order_id " +
            "WHERE o.student_id = ? ORDER BY o.order_id DESC", studentId);
    }

    public static DefaultTableModel items(int orderId) throws SQLException {
        return Db.table("SELECT f.food_name AS item, d.quantity AS qty, d.unit_price AS price, d.line_total AS subtotal " +
                "FROM order_details d JOIN food_items f ON f.food_id = d.food_id WHERE d.order_id = ? ORDER BY d.detail_id", orderId);
    }
}
