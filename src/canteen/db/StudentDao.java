package canteen.db;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

public class StudentDao {
    public static DefaultTableModel table(String search) throws SQLException {
        String s = "%" + search.toLowerCase() + "%";
        return Db.table("SELECT student_id, roll_no, full_name, email, phone, department FROM students " +
                "WHERE LOWER(full_name) LIKE ? OR LOWER(roll_no) LIKE ? OR LOWER(email) LIKE ? OR LOWER(department) LIKE ? " +
                "ORDER BY student_id", s, s, s, s);
    }

    public static void add(String roll, String name, String email, String phone, String dept) throws SQLException {
        Db.update("INSERT INTO students (roll_no, full_name, email, phone, department) VALUES (?,?,?,?,?)",
                roll, name, email, phone, dept.isEmpty() ? "General" : dept);
    }

    public static void update(int id, String roll, String name, String email, String phone, String dept) throws SQLException {
        Db.update("UPDATE students SET roll_no=?, full_name=?, email=?, phone=?, department=? WHERE student_id=?",
                roll, name, email, phone, dept, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM students WHERE student_id=?", id);
    }

    public static List<Object[]> get(int id) throws SQLException {
        return Db.rows("SELECT roll_no, full_name, email, phone, department FROM students WHERE student_id=?", id);
    }
}
