package canteen.ui;

import canteen.db.StudentDao;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Owner: student management (add, view, update, delete, search). */
public class StudentsPage extends BasePage {
    private final JTable table = new JTable();
    private final JTextField id = Theme.field(14), roll = Theme.field(14), name = Theme.field(14),
            email = Theme.field(14), phone = Theme.field(14), dept = Theme.field(14), search = Theme.field(20);

    public StudentsPage() {
        id.setEditable(false);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (e.getValueIsAdjusting() || r < 0) return;
            id.setText(String.valueOf(table.getValueAt(r, 0)));
            roll.setText(val(r, 1)); name.setText(val(r, 2)); email.setText(val(r, 3));
            phone.setText(val(r, 4)); dept.setText(val(r, 5));
        });
        JPanel form = Theme.card();
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(330, 0));
        Theme.formRow(form, 0, "ID", id);
        Theme.formRow(form, 1, "Roll no", roll);
        Theme.formRow(form, 2, "Full name", name);
        Theme.formRow(form, 3, "Email", email);
        Theme.formRow(form, 4, "Phone", phone);
        Theme.formRow(form, 5, "Department", dept);
        Theme.RoundButton add = Theme.button("Add Student", Theme.BLUE), upd = Theme.button("Update Student", Theme.GREEN),
                del = Theme.button("Delete Student", Theme.RED), clr = Theme.button("Clear", new Color(0x6C7A89));
        add.addActionListener(e -> save(true));
        upd.addActionListener(e -> save(false));
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clear());
        int row = 6;
        for (Theme.RoundButton b : new Theme.RoundButton[]{add, upd, del, clr}) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = row++; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5, 0, 5, 0);
            form.add(b, c);
        }
        GridBagConstraints fill = new GridBagConstraints();
        fill.gridx = 0; fill.gridy = row; fill.weighty = 1;
        form.add(new JPanel() {{ setOpaque(false); }}, fill);

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        JPanel s = new JPanel(new BorderLayout(8, 0));
        s.setOpaque(false);
        s.add(Theme.label("Search", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        s.add(search, BorderLayout.CENTER);
        Theme.RoundButton go = Theme.button("Search", Theme.NAVY);
        go.addActionListener(e -> refresh());
        search.addActionListener(e -> refresh());
        s.add(go, BorderLayout.EAST);
        right.add(s, BorderLayout.NORTH);
        right.add(Theme.scroll(table), BorderLayout.CENTER);
        add(form, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private String val(int r, int c) { Object o = table.getValueAt(r, c); return o == null ? "" : o.toString(); }

    @Override public void refresh() {
        try { table.setModel(StudentDao.table(search.getText().trim())); } catch (SQLException e) { error(e); }
    }

    private void save(boolean isNew) {
        if (roll.getText().trim().isEmpty() || name.getText().trim().isEmpty() || email.getText().trim().isEmpty()) {
            info("Roll no, name and email are required."); return;
        }
        if (!email.getText().contains("@")) { info("Enter a valid email."); return; }
        if (!isNew && id.getText().isEmpty()) { info("Select a student from the table first."); return; }
        try {
            if (isNew) StudentDao.add(roll.getText().trim(), name.getText().trim(), email.getText().trim(), phone.getText().trim(), dept.getText().trim());
            else StudentDao.update(Integer.parseInt(id.getText()), roll.getText().trim(), name.getText().trim(), email.getText().trim(), phone.getText().trim(), dept.getText().trim());
            refresh(); clear();
        } catch (SQLException e) { error(e); }
    }

    private void delete() {
        if (id.getText().isEmpty()) { info("Select a student first."); return; }
        if (!confirm("Delete student \"" + name.getText() + "\" and their login account?")) return;
        try { StudentDao.delete(Integer.parseInt(id.getText())); refresh(); clear(); } catch (SQLException e) { error(e); }
    }

    private void clear() {
        id.setText(""); roll.setText(""); name.setText(""); email.setText(""); phone.setText(""); dept.setText("");
        table.clearSelection();
    }
}
