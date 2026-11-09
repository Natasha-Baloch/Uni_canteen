package canteen.db;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

public class CategoryDao {
    /** [category_id, category_name] */
    public static List<Object[]> list() throws SQLException {
        return Db.rows("SELECT category_id, category_name FROM categories ORDER BY category_id");
    }

    public static DefaultTableModel table() throws SQLException {
        return Db.table("SELECT c.category_id, c.category_name, IFNULL(c.description,'-') AS description, " +
                "(SELECT COUNT(*) FROM food_items f WHERE f.category_id = c.category_id) AS food_items " +
                "FROM categories c ORDER BY c.category_id");
    }

    public static void add(String name, String desc) throws SQLException {
        Db.update("INSERT INTO categories (category_name, description) VALUES (?,?)", name, desc);
    }

    public static void update(int id, String name, String desc) throws SQLException {
        Db.update("UPDATE categories SET category_name=?, description=? WHERE category_id=?", name, desc, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM categories WHERE category_id=?", id);
    }
}
