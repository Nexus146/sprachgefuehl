import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Erzeugt die Store-Grafiken pixelgenau: Icon 512x512 und Feature-Grafik 1024x500.
 * Aufruf aus dem Projektordner:  java tools/StoreGraphics.java
 * Die Formen sind dieselben wie im adaptiven Icon (ic_launcher_foreground.xml).
 */
public class StoreGraphics {

    static final Color CREAM = new Color(0xFBF7F1);
    static final Color SAGE = new Color(0x3F6B4F);
    static final Color TERRA = new Color(0xB85C33);
    static final Color INK = new Color(0x23201C);
    static final Color INK_SOFT = new Color(0x5A544C);

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.GERMANY);
        new File("store").mkdirs();
        icon(512, new File("store/icon-512.png"));
        feature(new File("store/feature-1024x500.png"));
        System.out.println("geschrieben: store/icon-512.png, store/feature-1024x500.png");
    }

    /** Sprechblase mit Blatt und zwei Textzeilen, gezeichnet im 108er-Koordinatensystem des Icons. */
    static void drawMark(Graphics2D g, double scale, double offsetX, double offsetY) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform old = g.getTransform();
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);

        Path2D bubble = new Path2D.Double();
        bubble.moveTo(54, 26);
        bubble.curveTo(37.4, 26, 24, 36.5, 24, 49.5);
        bubble.curveTo(24, 56.9, 28.4, 63.5, 35.2, 67.8);
        bubble.lineTo(32.8, 78.4);
        bubble.curveTo(32.5, 79.8, 34.0, 80.9, 35.2, 80.1);
        bubble.lineTo(47.3, 72.0);
        bubble.curveTo(49.5, 72.4, 51.7, 72.6, 54.0, 72.6);
        bubble.curveTo(70.6, 72.6, 84.0, 62.1, 84.0, 49.1);
        bubble.curveTo(84.0, 36.1, 70.6, 26, 54, 26);
        bubble.closePath();
        g.setColor(SAGE);
        g.fill(bubble);

        Path2D leaf = new Path2D.Double();
        leaf.moveTo(54, 40);
        leaf.curveTo(54, 40, 66, 42, 66, 52);
        leaf.curveTo(66, 52, 54, 51, 54, 40);
        leaf.closePath();
        g.setColor(TERRA);
        g.fill(leaf);

        g.setColor(CREAM);
        g.fill(new Rectangle2D.Double(44, 50, 20, 4));
        g.fill(new Rectangle2D.Double(44, 58, 14, 4));

        g.setTransform(old);
    }

    static void icon(int size, File out) throws Exception {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(CREAM);
        g.fillRect(0, 0, size, size);
        double scale = size / 108.0;
        drawMark(g, scale, 0, 0);
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    static void feature(File out) throws Exception {
        int w = 1024, h = 500;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(CREAM);
        g.fillRect(0, 0, w, h);

        double markSize = 236;
        drawMark(g, markSize / 108.0, 72, (h - markSize) / 2.0);

        int textLeft = (int) (72 + markSize + 56);

        g.setColor(INK);
        g.setFont(new Font("SansSerif", Font.BOLD, 76));
        // Umlaute als Escapes: so hängt die Grafik nicht von der Kodierung der Quelldatei ab.
        g.drawString("Sprachgef\u00fchl", textLeft, 214);

        g.setColor(SAGE);
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        g.drawString("Englisch verstehen. Selbst sprechen.", textLeft, 266);

        g.setColor(INK_SOFT);
        g.setFont(new Font("SansSerif", Font.PLAIN, 23));
        g.drawString("Ruhige Lektionen f\u00fcr den Alltag \u00b7 offline nutzbar", textLeft, 316);
        g.drawString("ohne Konto, ohne Werbung, ohne Zeitdruck", textLeft, 348);

        g.dispose();
        ImageIO.write(img, "png", out);
    }
}
