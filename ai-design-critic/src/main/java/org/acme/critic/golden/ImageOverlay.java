package org.acme.critic.golden;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageOverlay {

    private ImageOverlay() {
    }

    public static void draw(BufferedImage img) {
        draw(img, Color.RED, 3f, 0.9f, 12, 0.04f, true);
    }

    public static void draw(BufferedImage img,
            Color color,
            float strokeW,
            float alpha,
            int minPiece,
            float marginFrac,
            boolean drawLabels) {
        if (img == null)
            return;

        final double PHI = (1 + Math.sqrt(5.0)) / 2.0;

        int iw = img.getWidth(), ih = img.getHeight();
        Graphics2D g = img.createGraphics();

        class Sq {
            final double x, y, s;
            final int side;

            Sq(double x, double y, double s, int side) {
                this.x = x;
                this.y = y;
                this.s = s;
                this.side = side;
            }
        }
        List<Sq> squares = new ArrayList<>();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(color);

            // --- fit BOTH orientations and pick the bigger one (always fits)
            int margin = Math.max(4, Math.round(Math.min(iw, ih) * marginFrac));
            double aw = iw - 2.0 * margin, ah = ih - 2.0 * margin;

            // Landscape golden rectangle (w = h * PHI)
            double hL = Math.min(ah, aw / PHI);
            double wL = hL * PHI;

            // Portrait golden rectangle (h = w * PHI)
            double wP = Math.min(aw, ah / PHI);
            double hP = wP * PHI;

            boolean landscape = (wL * hL >= wP * hP);
            double rw = landscape ? wL : wP;
            double rh = landscape ? hL : hP;

            double rx = (iw - rw) * 0.5;
            double ry = (ih - rh) * 0.5;

            // frame
            drawRect(g, rx, ry, rw, rh);

            // start side: LEFT for landscape, TOP for portrait
            int side = landscape ? 0 : 1; // 0=LEFT,1=TOP,2=RIGHT,3=BOTTOM

            // remaining rectangle
            double x = rx, y = ry, w = rw, h = rh;

            while (Math.min(w, h) >= minPiece) {
                double s, sx, sy;
                switch (side) {
                    case 0: // LEFT
                        s = h;
                        sx = x;
                        sy = y;
                        drawRect(g, sx, sy, s, s);
                        drawArcForSquare(g, sx, sy, s, side);
                        drawLine(g, x + s, y, x + s, y + h);
                        squares.add(new Sq(sx, sy, s, side));
                        x += s;
                        w -= s;
                        break;
                    case 1: // TOP
                        s = w;
                        sx = x;
                        sy = y;
                        drawRect(g, sx, sy, s, s);
                        drawArcForSquare(g, sx, sy, s, side);
                        drawLine(g, x, y + s, x + w, y + s);
                        squares.add(new Sq(sx, sy, s, side));
                        y += s;
                        h -= s;
                        break;
                    case 2: // RIGHT
                        s = h;
                        sx = x + (w - s);
                        sy = y;
                        drawRect(g, sx, sy, s, s);
                        drawArcForSquare(g, sx, sy, s, side);
                        drawLine(g, x + (w - s), y, x + (w - s), y + h);
                        squares.add(new Sq(sx, sy, s, side));
                        w -= s;
                        break;
                    default: // 3: BOTTOM
                        s = w;
                        sx = x;
                        sy = y + (h - s);
                        drawRect(g, sx, sy, s, s);
                        drawArcForSquare(g, sx, sy, s, side);
                        drawLine(g, x, y + (h - s), x + w, y + (h - s));
                        squares.add(new Sq(sx, sy, s, side));
                        h -= s;
                        break;
                }
                side = (side + 1) % 4;
            }

            // labels (largest → smallest)
            if (drawLabels && !squares.isEmpty()) {
                int n = squares.size();
                java.util.List<Integer> fib = new ArrayList<>(n);
                fib.add(1);
                if (n > 1)
                    fib.add(1);
                while (fib.size() < n) {
                    int k = fib.size();
                    fib.add(fib.get(k - 1) + fib.get(k - 2));
                }
                for (int i = 0; i < n; i++) {
                    Sq sq = squares.get(i);
                    int label = fib.get(n - 1 - i);
                    int fontPx = Math.max(10, (int) Math.round(sq.s * 0.25));
                    Font f = new Font(Font.SANS_SERIF, Font.PLAIN, fontPx);
                    g.setFont(f);
                    FontMetrics fm = g.getFontMetrics(f);
                    String text = Integer.toString(label);
                    int tx = ir(sq.x + (sq.s - fm.stringWidth(text)) / 2.0);
                    int ty = ir(sq.y + (sq.s - fm.getHeight()) / 2.0 + fm.getAscent());
                    g.drawString(text, tx, ty);
                }
            }

        } finally {
            g.dispose();
        }
    }

    // ---------- helpers ----------
    private static int ir(double v) {
        return (int) Math.round(v);
    }

    private static void drawRect(Graphics2D g, double x, double y, double w, double h) {
        g.drawRect(ir(x), ir(y), ir(w), ir(h));
    }

    private static void drawLine(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.drawLine(ir(x1), ir(y1), ir(x2), ir(y2));
    }

    /**
     * Quarter-circle, clockwise, for the square at (sx,sy) with side s.
     * Center at the interior corner; start angles follow edge midpoints:
     * LEFT → center (sx+s, sy+s), start 180°, extent -90°
     * TOP → center (sx, sy+s), start 90°, extent -90°
     * RIGHT → center (sx, sy), start 0°, extent -90°
     * BOTTOM → center (sx+s, sy), start 270°, extent -90°
     */
    private static void drawArcForSquare(Graphics2D g, double sx, double sy, double s, int side) {
        double cx, cy;
        int start;
        switch (side) {
            case 0:
                cx = sx + s;
                cy = sy + s;
                start = 180;
                break; // LEFT
            case 1:
                cx = sx;
                cy = sy + s;
                start = 90;
                break; // TOP
            case 2:
                cx = sx;
                cy = sy;
                start = 0;
                break; // RIGHT
            default:
                cx = sx + s;
                cy = sy;
                start = 270;
                break; // BOTTOM
        }
        int bx = ir(cx - s), by = ir(cy - s), d = ir(2 * s);
        g.drawArc(bx, by, d, d, start, -90);
    }
}