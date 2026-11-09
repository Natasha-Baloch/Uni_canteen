package canteen.ui;

import canteen.db.Db;
import canteen.db.UserDao;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** New customer sign-up (creates a STUDENTS row + a USERS row). */
public class RegisterDialog extends JDialog {
    private final JTextField name = Theme.field(20), roll = Theme.field(20), email = Theme.field(20),
            phone = Theme.field(20), dept = Theme.field(20), user = Theme.field(20);
    private final JPasswordField pass = Theme.password(20), pass2 = Theme.password(20);

    public RegisterDialog(JFrame owner) {
        super(owner, "Create customer account", true);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);
        Theme.formRow(p, 0, "Full name", name);
        Theme.formRow(p, 1, "Roll no", roll);
        Theme.formRow(p, 2, "Email", email);
        Theme.formRow(p, 3, "Phone", phone);
        Theme.formRow(p, 4, "Department", dept);
        Theme.formRow(p, 5, "Username", user);
        Theme.formRow(p, 6, "Password", pass);
        Theme.formRow(p, 7, "Confirm password", pass2);
        Theme.RoundButton ok = Theme.button("Register", Theme.YELLOW, Theme.BROWN);
        ok.addActionListener(e -> register());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 8; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(14, 0, 0, 0);
        p.add(ok, g);
        setContentPane(p);
        pack();
        setLocationRelativeTo(owner);
    }

    private void register() {
        String n = name.getText().trim(), r = roll.getText().trim(), em = email.getText().trim(), u = user.getText().trim();
        String pw = new String(pass.getPassword());
        if (n.isEmpty() || r.isEmpty() || em.isEmpty() || u.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, roll no, email, username and password are required."); return;
        }
        if (!em.contains("@")) { JOptionPane.showMessageDialog(this, "Enter a valid email."); return; }
        if (pw.length() < 6) { JOptionPane.showMessageDialog(this, "Password must be at least 6 characters."); return; }
        if (!pw.equals(new String(pass2.getPassword()))) { JOptionPane.showMessageDialog(this, "Passwords do not match."); return; }
        try {
            UserDao.registerCustomer(r, n, em, phone.getText().trim(), dept.getText().trim(), u, pw);
            JOptionPane.showMessageDialog(this, "Account created! You can log in now.");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, Db.friendly(ex), "Could not register", JOptionPane.ERROR_MESSAGE);
        }
    }
}
