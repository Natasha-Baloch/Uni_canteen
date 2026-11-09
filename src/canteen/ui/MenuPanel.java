package canteen.ui;

import canteen.db.*;
import canteen.model.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/** Customer menu: search + category chips + food cards (left) and the cart / checkout (right). */
public class MenuPanel extends JPanel {
    private final Map<Integer, CartItem> cart = new LinkedHashMap<>();
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JTextField search = Theme.field(24);
    private final JPanel cartList = new JPanel();
    private final JLabel totalLbl = Theme.label("Rs 0", Theme.H2, Theme.BROWN);
    private final JComboBox<String> payBox = new JComboBox<>(new String[]{"Pay by Cash at counter", "Pay Online (simulated)"});
    private final JTextField notes = Theme.field(14);
    private final Runnable onOrderPlaced;
    private int selectedCat = 0;

    public MenuPanel(Runnable onOrderPlaced) {
        this.onOrderPlaced = onOrderPlaced;
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // ---------- left: search + chips + grid ----------
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        search.putClientProperty("JTextField.placeholderText", "Search food...");
        top.add(wrapLabeled("Search", search), BorderLayout.NORTH);
        chips.setOpaque(false);
        top.add(chips, BorderLayout.SOUTH);
        left.add(top, BorderLayout.NORTH);

        grid.setOpaque(false);
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.add(grid, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(gridWrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        left.add(sp, BorderLayout.CENTER);
        add(left, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadFoods(); }
            public void removeUpdate(DocumentEvent e) { loadFoods(); }
            public void changedUpdate(DocumentEvent e) { loadFoods(); }
        });

        // ---------- right: cart ----------
        add(buildCart(), BorderLayout.EAST);

        loadChips();
        loadFoods();
        renderCart();
    }

    private JPanel wrapLabeled(String label, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(Theme.label(label, Theme.BOLD, Theme.MUTED), BorderLayout.WEST);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    // ================= categories =================
    private void loadChips() {
        chips.removeAll();
        try {
            addChip(0, "All");
            for (Object[] c : CategoryDao.list()) addChip(((Number) c[0]).intValue(), (String) c[1]);
        } catch (SQLException e) { showError(e); }
        chips.revalidate();
        chips.repaint();
    }

    private void addChip(int id, String name) {
        Theme.RoundButton b = Theme.button(name, id == selectedCat ? Theme.YELLOW : Color.WHITE,
                id == selectedCat ? Theme.BROWN : Theme.TEXT);
        b.addActionListener(e -> { selectedCat = id; loadChips(); loadFoods(); });
        chips.add(b);
    }

    // ================= food cards =================
    private void loadFoods() {
        grid.removeAll();
        try {
            List<Food> foods = FoodDao.list(search.getText().trim(), selectedCat);
            if (foods.isEmpty()) {
                grid.setLayout(new GridLayout(1, 1));
                grid.add(Theme.label("No food found.", Theme.H2, Theme.MUTED));
            } else {
                grid.setLayout(new GridLayout(0, 3, 16, 16));
                for (Food f : foods) grid.add(foodCard(f));
                // pad the last row so cards keep the same size
                while (grid.getComponentCount() % 3 != 0) { JPanel e = new JPanel(); e.setOpaque(false); grid.add(e); }
            }
        } catch (SQLException e) { showError(e); }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel foodCard(Food f) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(Theme.BORDER, 1, true));
        card.setPreferredSize(new Dimension(220, 285));
        card.add(new ImageLabel(f.imagePath, f.name, 220, 150), BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(10, 12, 4, 12));
        info.add(Theme.label(f.name, Theme.BOLD, Theme.TEXT));
        info.add(Theme.label(f.categoryName, Theme.SMALL, Theme.MUTED));
        String d = f.description.length() > 34 ? f.description.substring(0, 33) + "..." : f.description;
        info.add(Theme.label(d, Theme.SMALL, Theme.MUTED));
        card.add(info, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(0, 12, 12, 12));
        foot.add(Theme.label("Rs " + fmt(f.price), Theme.H2, Theme.BROWN), BorderLayout.WEST);
        if (f.available) {
            Theme.RoundButton add = Theme.button("+ Add", Theme.YELLOW, Theme.BROWN);
            add.addActionListener(e -> addToCart(f));
            foot.add(add, BorderLayout.EAST);
        } else {
            foot.add(Theme.label("SOLD OUT", Theme.BOLD, Theme.RED), BorderLayout.EAST);
        }
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    // ================= cart =================
    private JPanel buildCart() {
        JPanel box = Theme.card();
        box.setLayout(new BorderLayout(0, 10));
        box.setBackground(Theme.YELLOW_LIGHT);
        box.setPreferredSize(new Dimension(320, 0));
        box.add(Theme.label("Your Order", Theme.H2, Theme.BROWN), BorderLayout.NORTH);

        cartList.setLayout(new BoxLayout(cartList, BoxLayout.Y_AXIS));
        cartList.setOpaque(false);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(cartList, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        box.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JPanel tot = new JPanel(new BorderLayout());
        tot.setOpaque(false);
        tot.add(Theme.label("Total", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        tot.add(totalLbl, BorderLayout.EAST);
        bottom.add(tot);
        bottom.add(Box.createVerticalStrut(10));
        payBox.setAlignmentX(LEFT_ALIGNMENT);
        payBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        bottom.add(payBox);
        bottom.add(Box.createVerticalStrut(8));
        notes.setAlignmentX(LEFT_ALIGNMENT);
        notes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        notes.setToolTipText("Note for the kitchen (optional)");
        bottom.add(Theme.label("Note for the kitchen (optional)", Theme.SMALL, Theme.MUTED));
        bottom.add(notes);
        bottom.add(Box.createVerticalStrut(12));
        Theme.RoundButton place = Theme.button("Place Order", Theme.BROWN, Theme.YELLOW);
        place.setAlignmentX(LEFT_ALIGNMENT);
        place.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        place.addActionListener(e -> placeOrder());
        bottom.add(place);
        box.add(bottom, BorderLayout.SOUTH);
        return box;
    }

    private void addToCart(Food f) {
        CartItem it = cart.get(f.id);
        if (it == null) cart.put(f.id, new CartItem(f, 1));
        else if (it.qty < 20) it.qty++;
        renderCart();
    }

    private void renderCart() {
        cartList.removeAll();
        double total = 0;
        if (cart.isEmpty()) {
            JLabel l = Theme.label("<html>Your cart is empty.<br>Tap <b>+ Add</b> on a food card.</html>", Theme.BASE, Theme.MUTED);
            l.setBorder(new EmptyBorder(14, 0, 0, 0));
            cartList.add(l);
        }
        for (CartItem it : new ArrayList<>(cart.values())) {
            total += it.subtotal();
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(8, 10, 8, 10)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
            row.add(Theme.label("<html><b>" + it.food.name + "</b><br><span style='color:#777'>Rs " + fmt(it.subtotal()) + "</span></html>", Theme.BASE, Theme.TEXT), BorderLayout.CENTER);
            JPanel qty = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            qty.setOpaque(false);
            Theme.RoundButton minus = small("-");
            Theme.RoundButton plus = small("+");
            minus.addActionListener(e -> { it.qty--; if (it.qty <= 0) cart.remove(it.food.id); renderCart(); });
            plus.addActionListener(e -> { if (it.qty < 20) it.qty++; renderCart(); });
            qty.add(minus);
            qty.add(Theme.label(String.valueOf(it.qty), Theme.BOLD, Theme.TEXT));
            qty.add(plus);
            row.add(qty, BorderLayout.EAST);
            cartList.add(row);
            cartList.add(Box.createVerticalStrut(8));
        }
        totalLbl.setText("Rs " + fmt(total));
        cartList.revalidate();
        cartList.repaint();
    }

    private Theme.RoundButton small(String t) {
        Theme.RoundButton b = Theme.button(t, Theme.YELLOW, Theme.BROWN);
        b.setBorder(new EmptyBorder(2, 9, 2, 9));
        return b;
    }

    private void placeOrder() {
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(this, "Add some food to your cart first."); return; }
        String method = payBox.getSelectedIndex() == 0 ? "CASH" : "ONLINE";
        double total = 0;
        for (CartItem i : cart.values()) total += i.subtotal();
        String msg = "ONLINE".equals(method)
                ? "Pay Rs " + fmt(total) + " online now (simulated payment gateway) and send the order to the canteen?"
                : "Send this order (Rs " + fmt(total) + ") to the canteen? You will pay cash at the counter.";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirm order", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            int id = OrderDao.placeOrder(Session.studentId, new ArrayList<>(cart.values()), method, notes.getText().trim());
            cart.clear();
            notes.setText("");
            renderCart();
            JOptionPane.showMessageDialog(this, "Order #" + id + " sent to the canteen owner!\nWait for it to be accepted - you can track it in My Orders.",
                    "Order placed", JOptionPane.INFORMATION_MESSAGE);
            loadFoods();
            onOrderPlaced.run();
        } catch (SQLException e) { showError(e); }
    }

    private void showError(SQLException e) {
        JOptionPane.showMessageDialog(this, Db.friendly(e), "Problem", JOptionPane.ERROR_MESSAGE);
    }

    private static String fmt(double d) { return d == Math.floor(d) ? String.valueOf((long) d) : String.format("%.2f", d); }
}
