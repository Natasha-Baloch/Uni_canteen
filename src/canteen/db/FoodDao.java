package canteen.db;

import canteen.model.Food;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.*;

public class FoodDao {
    private static final String SELECT =
        "SELECT f.food_id, f.food_name, f.category_id, c.category_name, f.price, f.availability, f.image_path, f.description " +
        "FROM food_items f JOIN categories c ON c.category_id = f.category_id ";

    private static Food map(Object[] r) {
        Food f = new Food();
        f.id = ((Number) r[0]).intValue();
        f.name = (String) r[1];
        f.categoryId = ((Number) r[2]).intValue();
        f.categoryName = (String) r[3];
        f.price = ((Number) r[4]).doubleValue();
        f.available = "Y".equals(String.valueOf(r[5]).trim());
        f.imagePath = r[6] == null ? "" : (String) r[6];
        f.description = r[7] == null ? "" : (String) r[7];
        return f;
    }

    /** Food cards for the customer menu. categoryId 0 = all categories. */
    public static List<Food> list(String search, int categoryId) throws SQLException {
        List<Food> out = new ArrayList<>();
        for (Object[] r : Db.rows(SELECT + "WHERE (? = 0 OR f.category_id = ?) AND LOWER(f.food_name) LIKE ? " +
                "ORDER BY c.category_id, f.food_name", categoryId, categoryId, "%" + search.toLowerCase() + "%"))
            out.add(map(r));
        return out;
    }

    public static Food get(int id) throws SQLException {
        List<Object[]> r = Db.rows(SELECT + "WHERE f.food_id = ?", id);
        return r.isEmpty() ? null : map(r.get(0));
    }

    /** Table for the owner's Menu page. */
    public static DefaultTableModel table(String search) throws SQLException {
        String s = "%" + search.toLowerCase() + "%";
        return Db.table("SELECT f.food_id, f.food_name, c.category_name AS category, f.price, " +
                "CASE f.availability WHEN 'Y' THEN 'Available' ELSE 'Sold out' END AS availability, f.image_path AS image " +
                "FROM food_items f JOIN categories c ON c.category_id = f.category_id " +
                "WHERE LOWER(f.food_name) LIKE ? OR LOWER(c.category_name) LIKE ? ORDER BY f.food_id", s, s);
    }

    public static void add(String name, int cat, double price, boolean avail, String image, String desc) throws SQLException {
        Db.update("INSERT INTO food_items (food_name, category_id, price, availability, image_path, description) VALUES (?,?,?,?,?,?)",
                name, cat, price, avail ? "Y" : "N", image, desc);
    }

    public static void update(int id, String name, int cat, double price, boolean avail, String image, String desc) throws SQLException {
        Db.update("UPDATE food_items SET food_name=?, category_id=?, price=?, availability=?, image_path=?, description=? WHERE food_id=?",
                name, cat, price, avail ? "Y" : "N", image, desc, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM food_items WHERE food_id=?", id);
    }
}
