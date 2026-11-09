package canteen.ui;

import canteen.db.CategoryDao;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Owner: food categories (Fast Food, Snacks, Drinks, Meals, Desserts ...). */
public class CategoriesPage extends BasePage {
    private final JTable table = new JTable();
    private final JTextField id = Theme.field(14), name = Theme.field(14), desc = Theme.field(14);

    public CategoriesPage() {
        id.setEditable(false);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (e.getValueIsAdjusting() || r < 0) return;
            id.setText(String.valueOf(table.getValueAt(r, 0)));
            name.setText(String.valueOf(table.getValueAt(r, 1)));
            String d = String.valueOf(table.getValueAt(r, 2));
            desc.setText(d.equals("-") ? "" : d);
        });
        JPanel form = Theme.card();
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(320, 0));
        Theme.formRow(form, 0, "ID", id);
        Theme.formRow(form, 1, "Name", name);
        Theme.formRow(form, 2, "Description", desc);
        Theme.RoundButton add = Theme.button("Add Category", Theme.BLUE), upd = Theme.button("Update Category", Theme.GREEN),
                del = Theme.button("Delete Category", Theme.RED), clr = Theme.button("Clear", new Color(0x6C7A89));
        add.addActionListener(e -> save(true));
        upd.addActionListener(e -> save(false));
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clear());
        int row = 3;
        for (Theme.RoundButton b : new Theme.RoundButton[]{add, upd, del, clr}) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = row++; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5, 0, 5, 0);
            form.add(b, c);
        }
        GridBagConstraints fill = new GridBagConstraints();
        fill.gridx = 0; fill.gridy = row; fill.weighty = 1;
        form.add(new JPanel() {{ setOpaque(false); }}, fill);
        add(form, BorderLayout.WEST);
        add(Theme.scroll(table), BorderLayout.CENTER);
    }

    @Override public void refresh() {
        try { table.setModel(CategoryDao.table()); } catch (SQLException e) { error(e); }
    }

    private void save(boolean isNew) {
        if (name.getText().trim().isEmpty()) { info("Enter a category name."); return; }
        if (!isNew && id.getText().isEmpty()) { info("Select a category first."); return; }
        try {
            if (isNew) CategoryDao.add(name.getText().trim(), desc.getText().trim());
            else CategoryDao.update(Integer.parseInt(id.getText()), name.getText().trim(), desc.getText().trim());
            refresh(); clear();
        } catch (SQLException e) { error(e); }
    }

    private void delete() {
        if (id.getText().isEmpty()) { info("Select a category first."); return; }
        if (!confirm("Delete category \"" + name.getText() + "\"?")) return;
        try { CategoryDao.delete(Integer.parseInt(id.getText())); refresh(); clear(); } catch (SQLException e) { error(e); }
    }

    private void clear() { id.setText(""); name.setText(""); desc.setText(""); table.clearSelection(); }
}
