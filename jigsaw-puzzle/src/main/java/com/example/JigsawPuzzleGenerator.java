package com.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JigsawPuzzleGenerator {

    private final BufferedImage image;
    private final int rows;
    private final int cols;
    private final Random random = new Random();

    private int[][] horizontalEdges;
    private int[][] verticalEdges;

    public JigsawPuzzleGenerator(BufferedImage image, int rows, int cols) {
        this.image = image;
        this.rows = rows;
        this.cols = cols;
        generatePuzzlePattern();
    }

    private void generatePuzzlePattern() {
        horizontalEdges = new int[rows][cols - 1];
        verticalEdges = new int[rows - 1][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 1; c++) {
                horizontalEdges[r][c] = random.nextBoolean() ? 1 : -1;
            }
        }

        for (int r = 0; r < rows - 1; r++) {
            for (int c = 0; c < cols; c++) {
                verticalEdges[r][c] = random.nextBoolean() ? 1 : -1;
            }
        }
    }

    public BufferedImage generatePuzzle() {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();

        // High quality rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 1. Draw the base image
        g2.drawImage(image, 0, 0, null);

        double pieceWidth = (double) width / cols;
        double pieceHeight = (double) height / rows;
        double baseSize = Math.min(pieceWidth, pieceHeight);

        // --- COLLECT PATHS ---
        // Instead of drawing immediately, we collect all paths into a list.
        List<Path2D> allPaths = new ArrayList<>();

        // Generate Horizontal Paths
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 1; c++) {
                Path2D edge = new Path2D.Double();
                double x = (c + 1) * pieceWidth;
                double y1 = r * pieceHeight;
                double y2 = (r + 1) * pieceHeight;
                edge.moveTo(x, y1);
                long seed = (long) r * 12345 + c;
                createOrganicEdge(edge, x, y1, x, y2, horizontalEdges[r][c], baseSize, seed);
                allPaths.add(edge); // Add to list instead of drawing
            }
        }

        // Generate Vertical Paths
        for (int r = 0; r < rows - 1; r++) {
            for (int c = 0; c < cols; c++) {
                Path2D edge = new Path2D.Double();
                double x1 = c * pieceWidth;
                double x2 = (c + 1) * pieceWidth;
                double y = (r + 1) * pieceHeight;
                edge.moveTo(x1, y);
                long seed = (long) r * 67890 + c + 9999;
                createOrganicEdge(edge, x1, y, x2, y, verticalEdges[r][c], baseSize, seed);
                allPaths.add(edge); // Add to list instead of drawing
            }
        }

        // --- DRAWING PHASE ---
        // Enhanced embossed effect with highlight and shadow for natural depth

        // Multi-layer shadow for soft, natural falloff
        float[] shadowOffsets = {3.0f, 2.0f, 1.0f};
        int[] shadowAlphas = {40, 60, 80};
        
        for (int i = 0; i < shadowOffsets.length; i++) {
            g2.translate(shadowOffsets[i], shadowOffsets[i]);
            g2.setColor(new Color(0, 0, 0, shadowAlphas[i]));
            g2.setStroke(new BasicStroke(3.5f - i * 0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Path2D path : allPaths) {
                g2.draw(path);
            }
            g2.translate(-shadowOffsets[i], -shadowOffsets[i]);
        }

        // Highlight on opposite side (top-left) for bevel effect
        g2.translate(-1, -1);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Path2D path : allPaths) {
            g2.draw(path);
        }
        g2.translate(1, 1);

        // Inner glow for carved depth
        g2.translate(-0.5, -0.5);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Path2D path : allPaths) {
            g2.draw(path);
        }
        g2.translate(0.5, 0.5);

        // Main cut line
        g2.setColor(new Color(40, 40, 40, 200));
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Path2D path : allPaths) {
            g2.draw(path);
        }

        // Draw border (optional: you might want to shadow this too, but usually just the cuts)
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(0, 0, width - 1, height - 1);

        g2.dispose();
        return result;
    }

    private void createOrganicEdge(
            Path2D path,
            double x1, double y1,
            double x2, double y2,
            int tabType,
            double baseSize,
            long seed) {

        Random localRnd = new Random(seed);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);

        double ux = dx / len;
        double uy = dy / len;
        double vx = -uy * tabType;
        double vy = ux * tabType;

        // --- GEOMETRY CONFIGURATION ---
        double midPoint = 0.5 + (localRnd.nextDouble() * 0.1 - 0.05);

        // NARROW STEM configuration
        double tabHeight = baseSize * (0.24 + localRnd.nextDouble() * 0.04);
        double neckWidth = baseSize * (0.08 + localRnd.nextDouble() * 0.02); 
        double headWidth = baseSize * (0.28 + localRnd.nextDouble() * 0.05); 
        
        if (headWidth < neckWidth * 2.0) headWidth = neckWidth * 2.0;

        double baseGap = neckWidth * 2.2; 
        double waveAmp = baseSize * 0.02; 

        // --- POSITIONS ---
        double tStart = midPoint - (baseGap / 2.0) / len;
        double tEnd   = midPoint + (baseGap / 2.0) / len;

        double xStart = x1 + ux * (len * tStart);
        double yStart = y1 + uy * (len * tStart);
        double xEnd   = x1 + ux * (len * tEnd);
        double yEnd   = y1 + uy * (len * tEnd);

        // 1. Draw Wavy Line TO start
        drawWavyLine(path, x1, y1, xStart, yStart, ux, uy, vx, vy, waveAmp, localRnd);

        // --- MUSHROOM GENERATION ---
        double skew = (localRnd.nextDouble() - 0.5) * (baseSize * 0.05);

        double xPeak = x1 + ux * (len * midPoint) + vx * tabHeight + ux * skew;
        double yPeak = y1 + uy * (len * midPoint) + vy * tabHeight + uy * skew;

        double headLowOffset = tabHeight * 0.35; 
        double xHeadLeft = xPeak - ux * (headWidth / 2.0) - vx * headLowOffset;
        double yHeadLeft = yPeak - uy * (headWidth / 2.0) - vy * headLowOffset;

        double xHeadRight = xPeak + ux * (headWidth / 2.0) - vx * headLowOffset;
        double yHeadRight = yPeak + uy * (headWidth / 2.0) - vy * headLowOffset;

        // Left Side
        double cx1 = xStart + vx * (tabHeight * 0.15) + ux * (neckWidth * 0.2); 
        double cy1 = yStart + vy * (tabHeight * 0.15) + uy * (neckWidth * 0.2);
        double cx2 = xHeadLeft - vx * (tabHeight * 0.25);
        double cy2 = yHeadLeft - vy * (tabHeight * 0.25);
        path.curveTo(cx1, cy1, cx2, cy2, xHeadLeft, yHeadLeft);

        // Cap Left
        double cx3 = xHeadLeft + vx * (tabHeight * 0.25);
        double cy3 = yHeadLeft + vy * (tabHeight * 0.25);
        double cx4 = xPeak - ux * (headWidth * 0.25);
        double cy4 = yPeak - uy * (headWidth * 0.25);
        path.curveTo(cx3, cy3, cx4, cy4, xPeak, yPeak);

        // Cap Right
        double cx5 = xPeak + ux * (headWidth * 0.25);
        double cy5 = yPeak + uy * (headWidth * 0.25);
        double cx6 = xHeadRight + vx * (tabHeight * 0.25);
        double cy6 = yHeadRight + vy * (tabHeight * 0.25);
        path.curveTo(cx5, cy5, cx6, cy6, xHeadRight, yHeadRight);

        // Right Side
        double cx7 = xHeadRight - vx * (tabHeight * 0.25);
        double cy7 = yHeadRight - vy * (tabHeight * 0.25);
        double cx8 = xEnd + vx * (tabHeight * 0.15) - ux * (neckWidth * 0.2);
        double cy8 = yEnd + vy * (tabHeight * 0.15) - uy * (neckWidth * 0.2);
        path.curveTo(cx7, cy7, cx8, cy8, xEnd, yEnd);

        // 2. Draw Wavy Line FROM end
        drawWavyLine(path, xEnd, yEnd, x2, y2, ux, uy, vx, vy, waveAmp, localRnd);
    }

    private void drawWavyLine(
            Path2D path,
            double x1, double y1,
            double x2, double y2,
            double ux, double uy,
            double vx, double vy,
            double waveAmp,
            Random rnd) {
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        double segmentLen = Math.sqrt(dx * dx + dy * dy);
        
        double cp1x = x1 + ux * (segmentLen * 0.35) + vx * (rnd.nextBoolean() ? waveAmp : -waveAmp);
        double cp1y = y1 + uy * (segmentLen * 0.35) + vy * (rnd.nextBoolean() ? waveAmp : -waveAmp);
        double cp2x = x1 + ux * (segmentLen * 0.65) + vx * (rnd.nextBoolean() ? waveAmp : -waveAmp);
        double cp2y = y1 + uy * (segmentLen * 0.65) + vy * (rnd.nextBoolean() ? waveAmp : -waveAmp);
        
        path.curveTo(cp1x, cp1y, cp2x, cp2y, x2, y2);
    }
}