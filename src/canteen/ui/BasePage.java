package canteen.ui;

import canteen.db.Db;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Parent of every owner page: gives refresh() and tidy message helpers. */
public abstract class BasePage extends JPanel {
    public BasePage() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    }

    /** Reload data from MySQL. */
    public abstract void refresh();

    protected void error(Exception e) {
        String msg = e instanceof SQLException ? Db.friendly((SQLException) e) : String.valueOf(e.getMessage());
        JOptionPane.showMessageDialog(this, msg, "Problem", JOptionPane.ERROR_MESSAGE);
    }

    protected void info(String m) {
        JOptionPane.showMessageDialog(this, m, "Canteen", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirm(String m) {
        return JOptionPane.showConfirmDialog(this, m, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
