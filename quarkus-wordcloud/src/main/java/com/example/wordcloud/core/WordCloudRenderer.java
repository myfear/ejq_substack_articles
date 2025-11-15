package com.example.wordcloud.core;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * EdWordle-inspired Word Cloud Renderer for Quarkus.
 *
 * Large-font stability improvements:
 * - Max font limited to fraction of canvas height
 * - Global proportional downscale if needed
 * - Canvas-fit checks on placement
 * - Physics keeps bodies inside canvas
 * - Slight bounds inflation for big fonts (ascent/descent safety)
 */
public class WordCloudRenderer {

    private static final Logger LOG = Logger.getLogger(WordCloudRenderer.class);

    // Upper bound as % of canvas height for the largest word
    private static final double BIGGEST_WORD_HEIGHT_FRACTION = 0.25; // 25%
    private static final double SAFETY_MARGIN = 5.0;

    // Font cache: key = "family:size", value = Font instance
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();
    private static final Object FONT_CACHE_LOCK = new Object();

    // Thread-safe font name loading
    private static volatile String FONT_NAME;
    private static final Object FONT_LOAD_LOCK = new Object();

    public static String getFontName() {
        if (FONT_NAME == null) {
            synchronized (FONT_LOAD_LOCK) {
                if (FONT_NAME == null) {
                    FONT_NAME = loadAndRegisterFont();
                }
            }
        }
        return FONT_NAME;
    }

    private static String loadAndRegisterFont() {
        try (InputStream fontStream = WordCloudRenderer.class.getClassLoader()
                .getResourceAsStream("IBMPlexSans-Regular.ttf")) {
            if (fontStream != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                LOG.debugf("Successfully loaded and registered font: %s", customFont.getFamily());
                return customFont.getFamily();
            } else {
                LOG.warn("Font resource IBMPlexSans-Regular.ttf not found, using fallback font Arial");
            }
        } catch (Exception e) {
            LOG.warnf(e, "Failed to load IBMPlexSans-Regular.ttf, using fallback font Arial");
        }
        return "Arial"; // fallback
    }

    /**
     * Normalizes font family name, using default font if null/empty/Arial.
     */
    private static String normalizeFontFamily(String fontFamily) {
        if (fontFamily == null || fontFamily.isEmpty() || "Arial".equals(fontFamily)) {
            return getFontName();
        }
        return fontFamily;
    }

    /**
     * Gets or creates a Font instance, using cache to avoid repeated creation.
     */
    private static Font getFont(String fontFamily, int size) {
        String normalizedFamily = normalizeFontFamily(fontFamily);
        String key = normalizedFamily + ":" + size;
        
        Font cached = FONT_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        
        synchronized (FONT_CACHE_LOCK) {
            // Double-check after acquiring lock
            cached = FONT_CACHE.get(key);
            if (cached != null) {
                return cached;
            }
            
            Font font = new Font(normalizedFamily, Font.BOLD, size);
            FONT_CACHE.put(key, font);
            return font;
        }
    }

    private static void validateConfig(CloudConfig cfg) {
        if (cfg == null) {
            throw new IllegalArgumentException("CloudConfig cannot be null");
        }
        if (cfg.width <= 0) {
            throw new IllegalArgumentException("Width must be positive, got: " + cfg.width);
        }
        if (cfg.height <= 0) {
            throw new IllegalArgumentException("Height must be positive, got: " + cfg.height);
        }
        if (cfg.maxWords <= 0) {
            throw new IllegalArgumentException("maxWords must be positive, got: " + cfg.maxWords);
        }
        if (cfg.minFont <= 0) {
            throw new IllegalArgumentException("minFont must be positive, got: " + cfg.minFont);
        }
        if (cfg.maxFont <= 0) {
            throw new IllegalArgumentException("maxFont must be positive, got: " + cfg.maxFont);
        }
        if (cfg.minFont > cfg.maxFont) {
            throw new IllegalArgumentException(
                    String.format("minFont (%d) must be <= maxFont (%d)", cfg.minFont, cfg.maxFont));
        }
        if (cfg.rotateProb < 0.0 || cfg.rotateProb > 1.0) {
            throw new IllegalArgumentException(
                    "rotateProb must be between 0.0 and 1.0, got: " + cfg.rotateProb);
        }
        // Validate reasonable maximums to prevent memory issues
        if (cfg.width > 10000 || cfg.height > 10000) {
            throw new IllegalArgumentException(
                    String.format("Dimensions too large: %dx%d (max 10000x10000)", cfg.width, cfg.height));
        }
        if (cfg.maxWords > 1000) {
            throw new IllegalArgumentException("maxWords too large: " + cfg.maxWords + " (max 1000)");
        }
    }

    public static BufferedImage renderPng(List<String> tokens, CloudConfig cfg) {
        return renderPng(tokens, cfg, null);
    }

    public static BufferedImage renderPng(List<String> tokens, CloudConfig cfg, CancellationFlag cancellation) {
        // Validate configuration
        validateConfig(cfg);
        
        if (cancellation != null && cancellation.isCancelled()) {
            throw new IllegalStateException("Rendering was cancelled");
        }

        // 1) frequency
        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) {
            if (t == null)
                continue;
            String w = t.trim();
            if (!w.isEmpty())
                freq.put(w, freq.getOrDefault(w, 0) + 1);
        }

        // 2) select top N
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freq.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        int use = Math.min(cfg.maxWords, sorted.size());
        int maxFreq = use == 0 ? 1 : sorted.get(0).getValue();

        // 3) prepare image + measuring g2d
        BufferedImage img = new BufferedImage(cfg.width, cfg.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, cfg.width, cfg.height);
        Random rnd = new Random(cfg.seed);

        // 3.1 enforce sane max font relative to canvas height
        int canvasMaxFont = (int) Math.floor(cfg.height * BIGGEST_WORD_HEIGHT_FRACTION);
        int effectiveMaxFont = Math.min(cfg.maxFont, canvasMaxFont);

        // 4) words as bodies (create with preliminary font sizes)
        List<Body> bodies = new ArrayList<>();
        for (int i = 0; i < use; i++) {
            var e = sorted.get(i);
            double f = cfg.minFont + (e.getValue() / (double) maxFreq) * (effectiveMaxFont - cfg.minFont);
            // last-resort cap in case cfg.minFont > effectiveMaxFont
            f = Math.max(Math.min(f, effectiveMaxFont), Math.min(cfg.minFont, effectiveMaxFont));

            double rot = (cfg.rotateSome && rnd.nextDouble() < cfg.rotateProb) ? Math.PI / 2d : 0d;
            Color col = palette[i % palette.length];
            bodies.add(new Body(e.getKey(), e.getValue(), f, rot, col, cfg.fontFamily));
        }

        // 4.1 proportional downscale if biggest font still too tall (extra safety when
        // fonts differ)
        rescaleFontsToFit(bodies, cfg, g2d);

        // 5) initial spiral placement (Wordle-style) with canvas-fit check
        Point2D center = new Point2D.Double(cfg.width / 2.0, cfg.height / 2.0);
        for (Body b : bodies) {
            if (cancellation != null && cancellation.isCancelled()) {
                throw new IllegalStateException("Rendering was cancelled during spiral placement");
            }
            b.updateBounds(g2d);
            boolean placed = false;
            double angle = 0, radius = 0;
            // prevent infinite search if a word is still too big
            double maxRadius = Math.max(cfg.width, cfg.height) * 1.5;

            while (!placed && radius < maxRadius) {
                double x = center.getX() + radius * Math.cos(angle);
                double y = center.getY() + radius * Math.sin(angle);
                b.position = new Point2D.Double(x, y);
                b.updateBounds(g2d);

                if (!collides(b, bodies) && fitsInCanvas(b, cfg)) {
                    placed = true;
                }

                angle += 0.30;
                if (angle > 2 * Math.PI) {
                    angle = 0;
                    radius += 5;
                }
            }

            // If we failed to place (super rare with caps), pin to center safely
            if (!placed) {
                b.position = center;
                b.updateBounds(g2d);
            }
        }

        // 6) physics compaction (EdWordle-style), with in-bounds clamping
        Physics.simulate(bodies, cfg, center, cancellation);

        // 7) optional local boundary re-layout to close gaps
        if (cfg.localRewordle)
            LocalRewordle.compactBoundary(bodies, cfg, center, g2d);

        // 8) final recenter
        recenter(bodies, cfg, g2d);

        // 9) paint words
        g2d.setComposite(AlphaComposite.SrcOver);
        for (Body b : bodies)
            b.render(g2d);
        g2d.dispose();
        return img;
    }

    // ==== helpers ====

    private static boolean collides(Body b, List<Body> bodies) {
        for (Body o : bodies) {
            if (o == b || o.wordBox == null || b.wordBox == null)
                continue;
            if (!b.wordBox.intersects(o.wordBox))
                continue;
            if (b.twoLevel && o.twoLevel && b.letterBoxes != null && o.letterBoxes != null) {
                for (Rectangle2D r1 : b.letterBoxes)
                    for (Rectangle2D r2 : o.letterBoxes)
                        if (r1.intersects(r2))
                            return true;
                continue;
            }
            return true;
        }
        return false;
    }

    private static boolean fitsInCanvas(Body b, CloudConfig cfg) {
        if (b.wordBox == null)
            return false;
        return b.wordBox.getMinX() >= SAFETY_MARGIN &&
                b.wordBox.getMinY() >= SAFETY_MARGIN &&
                b.wordBox.getMaxX() <= cfg.width - SAFETY_MARGIN &&
                b.wordBox.getMaxY() <= cfg.height - SAFETY_MARGIN;
    }

    private static void recenter(List<Body> bodies, CloudConfig cfg, Graphics2D g) {
        if (bodies.isEmpty())
            return;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Body b : bodies) {
            Rectangle2D r = b.wordBox;
            minX = Math.min(minX, r.getMinX());
            minY = Math.min(minY, r.getMinY());
            maxX = Math.max(maxX, r.getMaxX());
            maxY = Math.max(maxY, r.getMaxY());
        }
        double dx = (cfg.width - (maxX - minX)) / 2 - minX;
        double dy = (cfg.height - (maxY - minY)) / 2 - minY;
        for (Body b : bodies) {
            b.position = new Point2D.Double(b.position.getX() + dx, b.position.getY() + dy);
            b.updateBounds(g);
        }
    }

    private static void rescaleFontsToFit(List<Body> bodies, CloudConfig cfg, Graphics2D g) {
        if (bodies.isEmpty())
            return;

        // Measure a temporary max height from text layout
        double maxTextHeight = 0.0;
        for (Body b : bodies) {
            b.updateBounds(g);
            if (b.wordBox != null) {
                maxTextHeight = Math.max(maxTextHeight, b.wordBox.getHeight());
            }
        }
        double targetMaxHeight = cfg.height * BIGGEST_WORD_HEIGHT_FRACTION;

        if (maxTextHeight > targetMaxHeight) {
            double scale = targetMaxHeight / maxTextHeight;
            for (Body b : bodies) {
                b.fontSize = Math.max(cfg.minFont, b.fontSize * scale);
                // mass ~ size^2 keeps physics stable after scale
                b.mass = b.fontSize * b.fontSize;
                b.updateBounds(g);
            }
        }
    }

    // ==== config ====
    public static class CloudConfig {
        public int width, height, maxWords, minFont, maxFont;
        public boolean rotateSome, localRewordle;
        public double rotateProb;
        public String fontFamily;
        public long seed;
    }

    // ==== body (word) ====
    static class Body {
        final String text;
        final int frequency;
        double fontSize; // mutable to allow proportional rescaling
        final double rotation;
        final Color color;
        final String fontFamily;
        Point2D position = new Point2D.Double(0, 0);
        Rectangle2D wordBox;
        List<Rectangle2D> letterBoxes;
        boolean twoLevel;
        double mass = 1;
        Vec2 v = new Vec2(0, 0);

        Body(String text, int freq, double fontSize, double rotation, Color color, String fontFamily) {
            this.text = text;
            this.frequency = freq;
            this.fontSize = fontSize;
            this.rotation = rotation;
            this.color = color;
            this.fontFamily = fontFamily;
            this.mass = fontSize * fontSize;
            this.twoLevel = fontSize >= 36; // ~0.5 of 72px; reasonable for big words
        }

        void updateBounds(Graphics2D g) {
            Font f = getFont(fontFamily, (int) Math.round(fontSize));
            FontRenderContext frc = g.getFontRenderContext();
            TextLayout tl = new TextLayout(text, f, frc);
            Rectangle2D b = tl.getBounds();

            // Slightly inflate for ascent/descent safety on very large fonts
            double inflate = Math.max(0, f.getSize2D() * 0.20);
            double w = b.getWidth() + inflate * 0.4;
            double h = b.getHeight() + inflate;

            if (rotation != 0) {
                double c = Math.abs(Math.cos(rotation)), s = Math.abs(Math.sin(rotation));
                double rw = w * c + h * s;
                double rh = w * s + h * c;
                w = rw;
                h = rh;
            }
            wordBox = new Rectangle2D.Double(position.getX() - w / 2, position.getY() - h / 2, w, h);

            letterBoxes = null;
            if (twoLevel) {
                letterBoxes = new ArrayList<>();
                FontMetrics fm = g.getFontMetrics(f);
                double x = position.getX() - w / 2;
                double lh = h;
                for (char ch : text.toCharArray()) {
                    double cw = fm.charWidth(ch) + inflate * 0.05;
                    letterBoxes.add(new Rectangle2D.Double(x, position.getY() - lh / 2, cw, lh));
                    x += cw;
                }
            }
        }

        void render(Graphics2D g) {
            Font f = getFont(fontFamily, (int) Math.round(fontSize));
            g.setFont(f);
            g.setColor(color);
            AffineTransform at = g.getTransform();
            g.rotate(rotation, position.getX(), position.getY());
            FontMetrics fm = g.getFontMetrics(f);
            float x = (float) (position.getX() - fm.stringWidth(text) / 2.0);
            float y = (float) (position.getY() + fm.getAscent() / 2.0 - fm.getDescent());
            g.drawString(text, x, y);
            g.setTransform(at);
        }
    }

    // ==== physics (neighbors + central force, damping) ====
    static class Physics {
        static void simulate(List<Body> bodies, CloudConfig cfg, Point2D center, CancellationFlag cancellation) {
            final int maxIt = 80;
            final double alpha = 0.1; // central pull weight
            final double beta = 1.0; // attenuation numerator
            final double lambda = 0.8;// velocity damping
            final double convergenceThreshold = 0.5; // early termination threshold
            final int convergenceCheckInterval = 5; // check every N iterations
            
            for (int t = 0; t < maxIt; t++) {
                if (cancellation != null && cancellation.isCancelled()) {
                    throw new IllegalStateException("Rendering was cancelled during physics simulation");
                }
                
                double maxVelocity = 0.0;
                
                for (Body b : bodies) {
                    Vec2 f = neighborForce(b, bodies).add(centralForce(b, center).scale(alpha));
                    double g = beta / (t + 1.0);
                    b.v = b.v.add(f.scale(g / b.mass));
                    b.v = b.v.scale(lambda);
                    maxVelocity = Math.max(maxVelocity, b.v.mag());
                }
                
                for (Body b : bodies) {
                    if (b.v.mag() > 0.1) {
                        b.position = new Point2D.Double(b.position.getX() + b.v.x, b.position.getY() + b.v.y);
                    }
                }

                // keep bodies inside canvas after each iteration
                clampInsideCanvas(bodies, cfg);

                // refresh bounds for next iteration
                BufferedImage m = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gm = m.createGraphics();
                gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Body b : bodies)
                    b.updateBounds(gm);
                gm.dispose();

                // simple separation if overlapping
                boolean hasOverlaps = false;
                for (int i = 0; i < bodies.size(); i++) {
                    Body a = bodies.get(i);
                    for (int j = i + 1; j < bodies.size(); j++) {
                        Body c = bodies.get(j);
                        if (a.wordBox != null && c.wordBox != null && a.wordBox.intersects(c.wordBox)) {
                            hasOverlaps = true;
                            double dx = c.position.getX() - a.position.getX();
                            double dy = c.position.getY() - a.position.getY();
                            double d = Math.hypot(dx, dy);
                            if (d > 0) {
                                Vec2 sep = new Vec2(dx / d, dy / d).scale(5.0);
                                a.position = new Point2D.Double(a.position.getX() - sep.x, a.position.getY() - sep.y);
                                c.position = new Point2D.Double(c.position.getX() + sep.x, c.position.getY() + sep.y);
                            }
                        }
                    }
                }
                
                // Early termination: check convergence periodically, but only if no overlaps
                // Also require minimum iterations to ensure proper separation
                if (t >= 20 && t > 0 && t % convergenceCheckInterval == 0 && maxVelocity < convergenceThreshold && !hasOverlaps) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Physics simulation converged early at iteration %d (max velocity: %.3f)", t, maxVelocity));
                    }
                    break;
                }
            }
        }

        static void clampInsideCanvas(List<Body> bodies, CloudConfig cfg) {
            for (Body b : bodies) {
                if (b.wordBox == null)
                    continue;
                double halfW = b.wordBox.getWidth() / 2.0;
                double halfH = b.wordBox.getHeight() / 2.0;
                double minX = SAFETY_MARGIN + halfW;
                double maxX = cfg.width - SAFETY_MARGIN - halfW;
                double minY = SAFETY_MARGIN + halfH;
                double maxY = cfg.height - SAFETY_MARGIN - halfH;

                double nx = Math.max(minX, Math.min(maxX, b.position.getX()));
                double ny = Math.max(minY, Math.min(maxY, b.position.getY()));

                if (nx != b.position.getX() || ny != b.position.getY()) {
                    b.position = new Point2D.Double(nx, ny);
                }
            }
        }

        static Vec2 neighborForce(Body b, List<Body> all) {
            Vec2 total = new Vec2(0, 0);
            for (Body n : all) {
                if (n == b)
                    continue;
                double dx = n.position.getX() - b.position.getX();
                double dy = n.position.getY() - b.position.getY();
                double dist2 = dx * dx + dy * dy + 1.0;
                double mag = (b.mass * n.mass) / dist2;
                Vec2 dir = new Vec2(dx, dy).norm();
                total = total.add(dir.scale(mag * 0.001));
            }
            return total;
        }

        static Vec2 centralForce(Body b, Point2D center) {
            double dx = center.getX() - b.position.getX();
            double dy = center.getY() - b.position.getY();
            double dist2 = dx * dx + dy * dy;
            return new Vec2(dx, dy).norm().scale(b.mass * dist2 * 0.00001);
        }
    }

    static class LocalRewordle {
        static void compactBoundary(List<Body> bodies, CloudConfig cfg, Point2D center, Graphics2D g) {
            double bw = bboxWidth(bodies), bh = bboxHeight(bodies);
            double radius = 0.8 * Math.min(bw, bh) / 2.0;
            List<Body> boundary = new ArrayList<>();
            for (Body b : bodies) {
                double dx = b.position.getX() - center.getX();
                double dy = b.position.getY() - center.getY();
                if (Math.hypot(dx, dy) > radius)
                    boundary.add(b);
            }
            boundary.sort(Comparator.comparingDouble(o -> -o.fontSize));
            for (Body b : boundary) {
                Point2D start = midpoint(b.position, center);
                int k = 20;
                double angle = 0, step = 0.25, rad = 0;
                Point2D best = null;
                int bestScore = -1;
                for (int i = 0; i < k; i++) {
                    double x = start.getX() + rad * Math.cos(angle);
                    double y = start.getY() + rad * Math.sin(angle);
                    Point2D p = new Point2D.Double(x, y);
                    Point2D old = b.position;
                    b.position = p;
                    b.updateBounds(g);
                    if (!collides(b, bodies)) {
                        int score = neighborCount(b, bodies);
                        if (score > bestScore) {
                            bestScore = score;
                            best = p;
                        }
                    }
                    b.position = old;
                    b.updateBounds(g);
                    angle += step;
                    if (angle > 2 * Math.PI) {
                        angle = 0;
                        rad += 6;
                    }
                }
                if (best != null) {
                    b.position = best;
                    b.updateBounds(g);
                }
            }
        }

        static int neighborCount(Body b, List<Body> all) {
            int n = 0;
            for (Body o : all) {
                if (o == b)
                    continue;
                if (b.wordBox.intersects(o.wordBox))
                    n++;
            }
            return n;
        }

        static double bboxWidth(List<Body> bodies) {
            double minX = Double.MAX_VALUE, maxX = -1;
            for (Body b : bodies) {
                Rectangle2D r = b.wordBox;
                minX = Math.min(minX, r.getMinX());
                maxX = Math.max(maxX, r.getMaxX());
            }
            return maxX - minX;
        }

        static double bboxHeight(List<Body> bodies) {
            double minY = Double.MAX_VALUE, maxY = -1;
            for (Body b : bodies) {
                Rectangle2D r = b.wordBox;
                minY = Math.min(minY, r.getMinY());
                maxY = Math.max(maxY, r.getMaxY());
            }
            return maxY - minY;
        }

        static Point2D midpoint(Point2D a, Point2D b) {
            return new Point2D.Double((a.getX() + b.getX()) / 2.0, (a.getY() + b.getY()) / 2.0);
        }
    }

    // ==== tiny vec ====
    static class Vec2 {
        final double x, y;

        Vec2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Vec2 add(Vec2 o) {
            return new Vec2(x + o.x, y + o.y);
        }

        Vec2 scale(double f) {
            return new Vec2(x * f, y * f);
        }

        double mag() {
            return Math.hypot(x, y);
        }

        Vec2 norm() {
            double m = mag();
            return m > 0 ? new Vec2(x / m, y / m) : new Vec2(0, 0);
        }
    }

    // ==== palette ====
    static final Color[] palette = {
            new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
            new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75)
    };

    // ==== cancellation support ====
    /**
     * Simple cancellation flag interface for long-running render operations.
     * Implementations should be thread-safe.
     */
    public interface CancellationFlag {
        /**
         * @return true if the operation should be cancelled
         */
        boolean isCancelled();
    }

    /**
     * Simple thread-safe cancellation flag implementation.
     */
    public static class SimpleCancellationFlag implements CancellationFlag {
        private volatile boolean cancelled = false;

        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

}