package canteen.db;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JDBC helper used by every DAO class.
 * Reads config/db.properties and opens a connection to MySQL.
 */
public class Db {
    private static Properties props;

    private static synchronized void load() {
        if (props != null) return;
        props = new Properties();
        try {
            File f = new File("config/db.properties");
            InputStream in = f.exists() ? new FileInputStream(f) : Db.class.getResourceAsStream("/db.properties");
            if (in == null) throw new RuntimeException("config/db.properties not found. Run the app from the project folder.");
            try (in) { props.load(in); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static volatile boolean initialized = false;

    /** Opens a connection and creates/initializes the database on first launch if it is empty. */
    public static Connection getConnection() throws SQLException {
        load();
        if (!initialized) initializeDatabase();
        return DriverManager.getConnection(props.getProperty("db.url"),
                props.getProperty("db.user"), props.getProperty("db.password"));
    }

    private static synchronized void initializeDatabase() throws SQLException {
        if (initialized) return;
        String url = props.getProperty("db.url", "").trim();
        String prefix = "jdbc:mysql://";
        if (!url.startsWith(prefix)) throw new SQLException("Invalid MySQL URL in config/db.properties: " + url);
        int slash = url.indexOf('/', prefix.length());
        if (slash < 0) throw new SQLException("Database name is missing from db.url in config/db.properties.");
        int query = url.indexOf('?', slash);
        String database = url.substring(slash + 1, query < 0 ? url.length() : query);
        if (!database.matches("[A-Za-z0-9_]+")) throw new SQLException("Invalid database name in db.url: " + database);
        String serverUrl = url.substring(0, slash) + (query < 0 ? "" : url.substring(query));
        String user = props.getProperty("db.user", "root").trim();
        String password = props.getProperty("db.password", "");

        try (Connection server = DriverManager.getConnection(serverUrl, user, password);
             Statement st = server.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        try (Connection c = DriverManager.getConnection(url, user, password)) {
            int tables;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type='BASE TABLE'" );
                 ResultSet rs = ps.executeQuery()) {
                rs.next(); tables = rs.getInt(1);
            }
            if (tables == 0) {
                runSqlFile(c, "sql/01_schema.sql");
                runSqlFile(c, "sql/02_sample_data.sql");
            }
        } catch (IOException ex) {
            throw new SQLException("Could not initialize the database. Keep the sql folder beside pom.xml and run the app from the UniCanteen project folder.", ex);
        }
        initialized = true;
    }

    private static void runSqlFile(Connection c, String relativePath) throws IOException, SQLException {
        File file = new File(relativePath);
        if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());
        StringBuilder cleaned = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("--")) cleaned.append(line).append('\n');
            }
        }
        try (Statement st = c.createStatement()) {
            for (String command : cleaned.toString().split(";")) {
                String sql = command.trim();
                if (sql.isEmpty() || sql.matches("(?is)^(CREATE\\s+DATABASE|USE)\\b.*")) continue;
                st.execute(sql);
            }
        }
    }

    private static void bind(PreparedStatement ps, Object... p) throws SQLException {
        for (int i = 0; i < p.length; i++) {
            if (p[i] == null) ps.setNull(i + 1, Types.VARCHAR);
            else ps.setObject(i + 1, p[i]);
        }
    }

    /** INSERT / UPDATE / DELETE. Returns rows affected. */
    public static int update(String sql, Object... p) throws SQLException {
        try (Connection c = getConnection()) {
            return update(c, sql, p);
        }
    }

    /** Same, but inside an existing connection (used for transactions). */
    public static int update(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            return ps.executeUpdate();
        }
    }

    /** SELECT -> list of rows. */
    public static List<Object[]> rows(String sql, Object... p) throws SQLException {
        List<Object[]> out = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] r = new Object[n];
                    for (int i = 0; i < n; i++) r[i] = rs.getObject(i + 1);
                    out.add(r);
                }
            }
        }
        return out;
    }

    /** SELECT a single value (first column of first row) or null. */
    public static Object scalar(String sql, Object... p) throws SQLException {
        List<Object[]> r = rows(sql, p);
        return r.isEmpty() ? null : r.get(0)[0];
    }

    public static double number(String sql, Object... p) throws SQLException {
        Object o = scalar(sql, p);
        return o == null ? 0 : ((Number) o).doubleValue();
    }

    /** SELECT -> Swing table model (column labels become headers). */
    public static DefaultTableModel table(String sql, Object... p) throws SQLException {
        Vector<String> cols = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                for (int i = 1; i <= n; i++) cols.add(md.getColumnLabel(i).replace('_', ' '));
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= n; i++) {
                        Object o = rs.getObject(i);
                        if (o instanceof java.util.Date) o = fmt.format((java.util.Date) o);
                        row.add(o);
                    }
                    data.add(row);
                }
            }
        }
        return new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /** Converts MySQL errors into messages a student can understand. */
    public static String friendly(SQLException e) {
        switch (e.getErrorCode()) {
            case 1062: return "That value already exists (username, roll no, email or name must be unique).";
            case 1451: return "This record is used by other data (for example existing orders), so it cannot be deleted.";
            case 3819: case 4025: return "A value is not allowed (check price, quantity, email and status values).";
            case 1048: return "A required field is empty.";
            case 1045: return "MySQL login failed. Put your actual MySQL username and password in config/db.properties (the password must not remain CHANGE_ME).";
            case 1049: return "MySQL could not select the database. Check the database name in config/db.properties and ensure your MySQL user can create databases.";
            case 0: return e.getMessage();
            default: return e.getMessage();
        }
    }
}
