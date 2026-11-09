package canteen;

import canteen.db.Db;
import canteen.ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                Db.getConnection().close();   // fail early with a clear message if MySQL is not reachable
            } catch (Exception e) {
                String m = e instanceof java.sql.SQLException ? Db.friendly((java.sql.SQLException) e) : e.getMessage();
                JOptionPane.showMessageDialog(null,
                    "Could not connect to the MySQL database.\n\n" + m +
                    "\n\nFIX: Start MySQL Server, then open config/db.properties and set db.user and db.password to your real MySQL login. Do not leave CHANGE_ME as the password.\nThe app automatically creates the unicanteen database and loads its tables and demo data on first launch (your MySQL account must have CREATE DATABASE permission).\nRun the app from the UniCanteen project folder.", "Database connection", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
