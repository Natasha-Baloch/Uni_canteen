package canteen.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows a food picture. image_path in the database can be
 *   - a file name inside the /images folder  (burger.jpg)
 *   - a full http(s) URL
 * While loading (or if the file is missing) a coloured placeholder with the food name is drawn,
 * so the screen NEVER shows a broken image.
 */
public class ImageLabel extends JLabel {
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    public ImageLabel(String path, String name, int w, int h) {
        setPreferredSize(new Dimension(w, h));
        setMinimumSize(new Dimension(w, h));
        setHorizontalAlignment(CENTER);
        setIcon(new ImageIcon(placeholder(name, w, h)));
        if (path == null || path.trim().isEmpty()) return;
        final String key = path + "@" + w + "x" + h;
        BufferedImage cached = CACHE.get(key);
        if (cached != null) { setIcon(new ImageIcon(cached)); return; }
        new SwingWorker<BufferedImage, Void>() {
            @Override protected BufferedImage doInBackground() {
                try {
                    BufferedImage src = read(path.trim());
                    return src == null ? null : cover(src, w, h);
                } catch (Exception e) {
                    return null;
                }
            }
            @Override protected void done() {
                try {
                    BufferedImage b = get();
                    if (b != null) { CACHE.put(key, b); setIcon(new ImageIcon(b)); }
                } catch (Exception ignored) { }
            }
        }.execute();
    }

    private static BufferedImage read(String path) throws Exception {
        if (path.startsWith("http")) {
            URLConnection c = java.net.URI.create(path).toURL().openConnection();
            c.setConnectTimeout(4000);
            c.setReadTimeout(7000);
            c.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = c.getInputStream()) { return ImageIO.read(in); }
        }
        File f = new File("images", path);
        if (!f.exists()) f = new File(path);
        if (f.exists()) return ImageIO.read(f);
        InputStream in = ImageLabel.class.getResourceAsStream("/images/" + path);
        if (in != null) try (in) { return ImageIO.read(in); }
        return null;
    }

    /** Scale to fill the box and crop the centre (like CSS object-fit: cover). */
    private static BufferedImage cover(BufferedImage src, int w, int h) {
        double s = Math.max((double) w / src.getWidth(), (double) h / src.getHeight());
        int sw = (int) Math.ceil(src.getWidth() * s), sh = (int) Math.ceil(src.getHeight() * s);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
        g.dispose();
        return out;
    }

    public static BufferedImage placeholder(String name, int w, int h) {
        String n = name == null ? "?" : name;
        float hue = (Math.abs(n.hashCode()) % 360) / 360f;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, Color.getHSBColor(hue, 0.45f, 1f), w, h, Color.getHSBColor(hue, 0.75f, 0.85f)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillOval(-w / 4, -h / 3, w, h);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(18, h / 3)));
        String ini = n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(ini, (w - fm.stringWidth(ini)) / 2, h / 2 + fm.getAscent() / 3);
        g.dispose();
        return img;
    }
}
