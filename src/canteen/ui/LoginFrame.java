package canteen.ui;

import canteen.db.Db;
import canteen.db.UserDao;
import canteen.model.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Login screen with a Customer / Owner switch. */
public class LoginFrame extends JFrame {
    private boolean ownerMode = false;
    private final Theme.RoundButton customerTab = Theme.button("Customer", Theme.YELLOW, Theme.BROWN);
    private final Theme.RoundButton ownerTab = Theme.button("Owner", new Color(0xE9ECEF), Theme.TEXT);
    private final JTextField user = Theme.field(18);
    private final JPasswordField pass = Theme.password(18);
    private final VideoPanel videoPanel;

    public LoginFrame() {
        setTitle("UniCanteen - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // full screen
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) { videoPanel.start(); }
        });
        setLayout(new GridLayout(1, 2));

        // ---- left brand panel ----
        videoPanel = new VideoPanel();
        JPanel left = videoPanel;
        left.setLayout(new GridBagLayout());
        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(Theme.label("UniCanteen.", new Font("Segoe UI", Font.BOLD, 52), Color.WHITE));
        txt.add(Box.createVerticalStrut(12));
        txt.add(Theme.label("Order. Pay. Pick up.", new Font("Segoe UI", Font.BOLD, 28), Color.WHITE));
        txt.add(Box.createVerticalStrut(20));
        for (String s : new String[]{"\u2022 Browse the menu with photos", "\u2022 Send your order to the canteen owner",
                                     "\u2022 Track it live until it is ready", "\u2022 Pay by cash or online"})
            txt.add(Theme.label(s, new Font("Segoe UI", Font.BOLD, 18), Color.WHITE));
        left.add(txt);

        // ---- right form ----
        JPanel right = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(0xC2561A), getWidth(), getHeight(), new Color(0xE8740F)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 184, 0, 50));
                g2.fillOval(getWidth() - 200, -140, 340, 340);
                g2.fillOval(-120, getHeight() - 180, 300, 300);
                g2.dispose();
            }
        };
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 40, 0, 40));

        form.add(Theme.label("Welcome back", Theme.H1, Color.WHITE));
        form.add(Box.createVerticalStrut(4));
        form.add(Theme.label("Sign in to continue", Theme.BOLD, new Color(0xFFF1D0)));
        form.add(Box.createVerticalStrut(18));

        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        tabs.add(customerTab);
        tabs.add(ownerTab);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        form.add(tabs);
        customerTab.addActionListener(e -> setMode(false));
        ownerTab.addActionListener(e -> setMode(true));

        form.add(Box.createVerticalStrut(18));
        form.add(Theme.label("Username", Theme.BOLD, Color.WHITE));
        form.add(Box.createVerticalStrut(4));
        user.setAlignmentX(LEFT_ALIGNMENT);
        form.add(user);
        form.add(Box.createVerticalStrut(12));
        form.add(Theme.label("Password", Theme.BOLD, Color.WHITE));
        form.add(Box.createVerticalStrut(4));
        pass.setAlignmentX(LEFT_ALIGNMENT);
        form.add(pass);
        form.add(Box.createVerticalStrut(18));

        Theme.RoundButton login = Theme.button("Login", Theme.YELLOW, Theme.BROWN);
        login.setAlignmentX(LEFT_ALIGNMENT);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        login.addActionListener(e -> doLogin());
        form.add(login);
        getRootPane().setDefaultButton(login);

        form.add(Box.createVerticalStrut(8));
        Theme.RoundButton reg = Theme.button("New student? Create an account", Color.WHITE, new Color(0xB5400F));
        reg.setAlignmentX(LEFT_ALIGNMENT);
        reg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        reg.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        form.add(reg);

        form.add(Box.createVerticalStrut(10));
        JLabel hint = Theme.label("<html>Demo logins &nbsp; Owner: <b>owner / owner123</b><br>Customer: <b>ali / student123</b></html>", Theme.BOLD, Color.WHITE);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        form.add(hint);

        right.add(form);
        add(left);
        add(right);
    }

    @Override public void dispose() {
        if (videoPanel != null) videoPanel.stop();   // stop video and sound when leaving the login screen
        super.dispose();
    }

    private void setMode(boolean owner) {
        ownerMode = owner;
        customerTab.setColor(owner ? new Color(0xE9ECEF) : Theme.YELLOW);
        customerTab.setForeground(owner ? Theme.TEXT : Theme.BROWN);
        ownerTab.setColor(owner ? Theme.YELLOW : new Color(0xE9ECEF));
        ownerTab.setForeground(owner ? Theme.BROWN : Theme.TEXT);
    }

    private void doLogin() {
        String u = user.getText().trim(), p = new String(pass.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }
        try {
            String role = UserDao.login(u, p);
            if (role == null) {
                JOptionPane.showMessageDialog(this, "Wrong username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ownerMode != role.equals("OWNER")) {
                Session.clear();
                JOptionPane.showMessageDialog(this, "This is a " + role.toLowerCase() + " account. Please choose the "
                        + role.toLowerCase() + " tab.", "Wrong role", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dispose();
            if (role.equals("OWNER")) new OwnerFrame().setVisible(true);
            else new CustomerFrame().setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, Db.friendly(ex), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
