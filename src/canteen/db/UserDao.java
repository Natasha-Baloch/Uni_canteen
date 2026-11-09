package canteen.db;

import canteen.model.Session;
import java.sql.*;
import java.util.List;

/** Login and customer registration. */
public class UserDao {

    /** Returns the role (OWNER / CUSTOMER) and fills Session, or null if the login is wrong. */
    public static String login(String username, String password) throws SQLException {
        List<Object[]> r = Db.rows(
            "SELECT user_id, role, student_id, full_name, username FROM users " +
            "WHERE LOWER(username) = LOWER(?) AND password_hash = ? AND is_active = 'Y'",
            username, Security.sha256(password));
        if (r.isEmpty()) return null;
        Object[] u = r.get(0);
        Session.userId = ((Number) u[0]).intValue();
        Session.role = (String) u[1];
        Session.studentId = u[2] == null ? 0 : ((Number) u[2]).intValue();
        Session.fullName = (String) u[3];
        Session.username = (String) u[4];
        return Session.role;
    }

    /** Creates a STUDENTS row and a USERS row in ONE transaction. */
    public static void registerCustomer(String roll, String name, String email, String phone,
                                        String dept, String username, String password) throws SQLException {
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                int studentId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO students (roll_no, full_name, email, phone, department) VALUES (?,?,?,?,?)",
                        new String[]{"STUDENT_ID"})) {
                    ps.setString(1, roll); ps.setString(2, name); ps.setString(3, email);
                    ps.setString(4, phone); ps.setString(5, dept.isEmpty() ? "General" : dept);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); studentId = rs.getInt(1); }
                }
                Db.update(c, "INSERT INTO users (username, password_hash, role, student_id, full_name) VALUES (?,?,?,?,?)",
                        username, Security.sha256(password), "CUSTOMER", studentId, name);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
