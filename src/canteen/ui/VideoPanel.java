package canteen.ui;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Plays a looping video (JPEG frames in media/frames + media/audio.wav) as a background.
 * The video fills the whole panel (cover mode) and the sound is kept in sync with the frames.
 * Children added to this panel are drawn on top of the video.
 */
public class VideoPanel extends JPanel {
    private static final double FPS = 24.0;
    private final File[] frames;
    private volatile BufferedImage current;
    private volatile boolean running;
    private Clip clip;
    private Thread worker;
    private final Color overlay = new Color(0, 0, 0, 110);   // darkens video so white text stays readable

    public VideoPanel() {
        File dir = new File("media/frames");
        File[] f = dir.listFiles((d, n) -> n.endsWith(".jpg"));
        if (f != null) java.util.Arrays.sort(f);
        frames = f == null ? new File[0] : f;
        setBackground(Theme.BROWN);
    }

    /** Start video + sound. Safe to call when the media folder is missing (shows plain background). */
    public void start() {
        if (running || frames.length == 0) return;
        running = true;
        try {
            File wav = new File("media/audio.wav");
            if (wav.exists()) {
                AudioInputStream in = AudioSystem.getAudioInputStream(wav);
                clip = AudioSystem.getClip();
                clip.open(in);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            clip = null;   // no sound device - video still plays
        }
        final long t0 = System.nanoTime();
        worker = new Thread(() -> {
            int last = -1;
            while (running) {
                double sec;
                if (clip != null && clip.getMicrosecondLength() > 0)
                    sec = (clip.getMicrosecondPosition() % clip.getMicrosecondLength()) / 1_000_000.0;
                else
                    sec = ((System.nanoTime() - t0) / 1e9) % (frames.length / FPS);
                int idx = Math.min(frames.length - 1, (int) (sec * FPS));
                if (idx != last) {
                    try {
                        current = ImageIO.read(frames[idx]);
                        last = idx;
                        repaint();
                    } catch (Exception ignored) { }
                } else {
                    try { Thread.sleep(5); } catch (InterruptedException e) { return; }
                }
            }
        }, "video-player");
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        running = false;
        if (clip != null) { clip.stop(); clip.close(); clip = null; }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        BufferedImage img = current;
        if (img != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            // "cover": scale so the video fills the entire panel, crop the overflow
            double s = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
            int dw = (int) Math.ceil(img.getWidth() * s), dh = (int) Math.ceil(img.getHeight() * s);
            g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            g2.setColor(overlay);
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
    }
}
