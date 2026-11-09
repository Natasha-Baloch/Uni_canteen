package canteen.db;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;

/** Dashboard numbers and report queries (JOIN, GROUP BY, HAVING, subqueries). */
public class ReportDao {
    public static final String[] REPORT_NAMES = {
        "Daily Sales", "Monthly Sales", "Popular Food Items", "Pending Payments", "Top Customers", "Sales by Category"
    };

    public static long count(String table) throws SQLException {
        return (long) Db.number("SELECT COUNT(*) FROM " + table);
    }
    public static double totalSales() throws SQLException {
        return Db.number("SELECT IFNULL(SUM(amount),0) FROM payments WHERE status = 'PAID'");
    }
    public static long pendingOrders() throws SQLException {
        return (long) Db.number("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'");
    }

    public static DefaultTableModel run(int index) throws SQLException {
        switch (index) {
            case 0: return Db.table(
                "SELECT DATE_FORMAT(DATE(p.paid_at),'%Y-%m-%d') AS sale_day, COUNT(*) AS paid_orders, SUM(p.amount) AS total_sales " +
                "FROM payments p WHERE p.status = 'PAID' GROUP BY DATE(p.paid_at) ORDER BY DATE(p.paid_at) DESC");
            case 1: return Db.table(
                "SELECT DATE_FORMAT(p.paid_at,'%Y-%m') AS sale_month, COUNT(*) AS paid_orders, SUM(p.amount) AS total_sales " +
                "FROM payments p WHERE p.status = 'PAID' GROUP BY DATE_FORMAT(p.paid_at,'%Y-%m') ORDER BY 1 DESC");
            case 2: return Db.table(
                "SELECT f.food_name, c.category_name AS category, SUM(d.quantity) AS qty_sold, SUM(d.line_total) AS revenue " +
                "FROM order_details d JOIN food_items f ON f.food_id = d.food_id " +
                "JOIN categories c ON c.category_id = f.category_id JOIN orders o ON o.order_id = d.order_id " +
                "WHERE o.status NOT IN ('REJECTED','CANCELLED') " +
                "GROUP BY f.food_name, c.category_name HAVING SUM(d.quantity) >= 1 ORDER BY qty_sold DESC, revenue DESC");
            case 3: return Db.table(
                "SELECT p.payment_id, o.order_id, s.full_name AS customer, s.phone, p.method, p.amount " +
                "FROM payments p JOIN orders o ON o.order_id = p.order_id JOIN students s ON s.student_id = o.student_id " +
                "WHERE p.status = 'PENDING' ORDER BY p.payment_id");
            case 4: return Db.table(
                "SELECT s.full_name AS customer, s.department, COUNT(o.order_id) AS orders_placed, SUM(o.total_amount) AS total_spent " +
                "FROM students s JOIN orders o ON o.student_id = s.student_id " +
                "WHERE o.status NOT IN ('REJECTED','CANCELLED') GROUP BY s.full_name, s.department " +
                "HAVING SUM(o.total_amount) > (SELECT AVG(total_amount) FROM orders) ORDER BY total_spent DESC");
            default: return Db.table(
                "SELECT c.category_name AS category, SUM(d.quantity) AS items_sold, SUM(d.line_total) AS revenue " +
                "FROM order_details d JOIN food_items f ON f.food_id = d.food_id JOIN categories c ON c.category_id = f.category_id " +
                "JOIN orders o ON o.order_id = d.order_id WHERE o.status NOT IN ('REJECTED','CANCELLED') " +
                "GROUP BY c.category_name ORDER BY revenue DESC");
        }
    }
}
