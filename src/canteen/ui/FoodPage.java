package canteen.ui;

import canteen.db.*;
import canteen.model.Food;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/** Owner: food management (add / update / delete, price, availability, image). */
public class FoodPage extends BasePage {
    private final JTable table = new JTable();
    private final JTextField id = Theme.field(14), name = Theme.field(14), price = Theme.field(14),
            image = Theme.field(14), desc = Theme.field(14), search = Theme.field(20);
    private final JComboBox<String> category = new JComboBox<>();
    private final JCheckBox available = new JCheckBox("Available", true);
    private final java.util.List<Integer> catIds = new ArrayList<>();
    private final JPanel preview = new JPanel(new BorderLayout());

    public FoodPage() {
        id.setEditable(false);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) fillForm(); });

        JPanel form = Theme.card();
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(330, 0));
        Theme.formRow(form, 0, "ID", id);
        Theme.formRow(form, 1, "Name", name);
        Theme.formRow(form, 2, "Category", category);
        Theme.formRow(form, 3, "Price (Rs)", price);
        Theme.formRow(form, 4, "", available);
        JPanel imgRow = new JPanel(new BorderLayout(4, 0));
        imgRow.setOpaque(false);
        imgRow.add(image, BorderLayout.CENTER);
        Theme.RoundButton browse = Theme.button("...", Theme.NAVY);
        browse.addActionListener(e -> browse());
        imgRow.add(browse, BorderLayout.EAST);
        Theme.formRow(form, 5, "Image", imgRow);
        Theme.formRow(form, 6, "Description", desc);

        preview.setOpaque(false);
        preview.setPreferredSize(new Dimension(220, 120));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 7; g.gridwidth = 2; g.insets = new Insets(8, 0, 8, 0);
        form.add(preview, g);

        Theme.RoundButton add = Theme.button("Add Item", Theme.BLUE);
        Theme.RoundButton upd = Theme.button("Update Item", Theme.GREEN);
        Theme.RoundButton del = Theme.button("Delete Item", Theme.RED);
        Theme.RoundButton clr = Theme.button("Clear", new Color(0x6C7A89));
        add.addActionListener(e -> save(true));
        upd.addActionListener(e -> save(false));
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clear());
        int row = 8;
        for (Theme.RoundButton b : new Theme.RoundButton[]{add, upd, del, clr}) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = row++; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(4, 0, 4, 0);
            form.add(b, c);
        }

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        JPanel s = new JPanel(new BorderLayout(8, 0));
        s.setOpaque(false);
        s.add(Theme.label("Search", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        s.add(search, BorderLayout.CENTER);
        Theme.RoundButton go = Theme.button("Search", Theme.NAVY);
        go.addActionListener(e -> reload());
        search.addActionListener(e -> reload());
        s.add(go, BorderLayout.EAST);
        right.add(s, BorderLayout.NORTH);
        right.add(Theme.scroll(table), BorderLayout.CENTER);

        add(form, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    @Override public void refresh() {
        try {
            category.removeAllItems();
            catIds.clear();
            for (Object[] c : CategoryDao.list()) { catIds.add(((Number) c[0]).intValue()); category.addItem((String) c[1]); }
        } catch (SQLException e) { error(e); }
        reload();
    }

    private void reload() {
        try { table.setModel(FoodDao.table(search.getText().trim())); } catch (SQLException e) { error(e); }
    }

    private void fillForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        try {
            Food f = FoodDao.get(((Number) table.getValueAt(r, 0)).intValue());
            if (f == null) return;
            id.setText(String.valueOf(f.id));
            name.setText(f.name);
            price.setText(String.valueOf(f.price));
            category.setSelectedIndex(Math.max(0, catIds.indexOf(f.categoryId)));
            available.setSelected(f.available);
            image.setText(f.imagePath);
            desc.setText(f.description);
            showPreview(f.imagePath, f.name);
        } catch (SQLException e) { error(e); }
    }

    private void showPreview(String path, String n) {
        preview.removeAll();
        preview.add(new ImageLabel(path, n, 220, 120), BorderLayout.CENTER);
        preview.revalidate();
        preview.repaint();
    }

    private void browse() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File src = fc.getSelectedFile();
        try {
            File dir = new File("images");
            dir.mkdirs();
            Files.copy(src.toPath(), new File(dir, src.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            image.setText(src.getName());
            showPreview(src.getName(), name.getText());
        } catch (Exception e) { error(e); }
    }

    private boolean valid() {
        if (name.getText().trim().isEmpty()) { info("Enter the food name."); return false; }
        if (category.getSelectedIndex() < 0) { info("Add a category first."); return false; }
        try {
            if (Double.parseDouble(price.getText().trim()) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) { info("Price must be a number greater than 0."); return false; }
        return true;
    }

    private void save(boolean isNew) {
        if (!valid()) return;
        if (!isNew && id.getText().isEmpty()) { info("Select an item from the table first."); return; }
        try {
            int cat = catIds.get(category.getSelectedIndex());
            double p = Double.parseDouble(price.getText().trim());
            if (isNew) FoodDao.add(name.getText().trim(), cat, p, available.isSelected(), image.getText().trim(), desc.getText().trim());
            else FoodDao.update(Integer.parseInt(id.getText()), name.getText().trim(), cat, p, available.isSelected(), image.getText().trim(), desc.getText().trim());
            reload();
            clear();
        } catch (SQLException e) { error(e); }
    }

    private void delete() {
        if (id.getText().isEmpty()) { info("Select an item from the table first."); return; }
        if (!confirm("Delete \"" + name.getText() + "\"?\n(If it was ordered before, mark it as not available instead.)")) return;
        try { FoodDao.delete(Integer.parseInt(id.getText())); reload(); clear(); } catch (SQLException e) { error(e); }
    }

    private void clear() {
        id.setText(""); name.setText(""); price.setText(""); image.setText(""); desc.setText("");
        available.setSelected(true);
        table.clearSelection();
        preview.removeAll();
        preview.repaint();
    }
}
