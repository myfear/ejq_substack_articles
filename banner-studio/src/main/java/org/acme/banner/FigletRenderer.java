
package org.acme.banner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.lalyos.jfiglet.FigletFont;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FigletRenderer {

    // Available fonts - try narrow-ish fonts first to fit within a max width
    public static final List<String> FONTS = List.of(
            "Small.flf", "ANSIRegular.flf", "Slant.flf", "Digital.flf");
    
    // Try narrow-ish fonts first to fit within a max width
    private static final List<String> FONT_ORDER = FONTS;

    public RenderResult renderFitting(String text, int maxWidth) throws IOException {
        text = sanitize(text);
        text = filterAscii(text);
        text = limitLength(text, 40);

        for (String fontName : FONT_ORDER) {
            try {
                String ascii = render(text, fontName);
                int width = measureWidth(ascii);
                if (width <= maxWidth) {
                    return new RenderResult(ascii, fontName, width, true);
                }
            } catch (Exception e) {
                // Try next font
            }
        }

        // Nothing fits: return the narrowest anyway and flag it
        try {
            String fallback = render(text, FONT_ORDER.get(0));
            return new RenderResult(fallback, FONT_ORDER.get(0), measureWidth(fallback), false);
        } catch (Exception e) {
            return new RenderResult("[Error rendering text]", FONT_ORDER.get(0), 0, false);
        }
    }

    public String renderWithFont(String text, String fontName) throws IOException {
        text = sanitize(text);
        text = filterAscii(text);
        text = limitLength(text, 40);
        try {
            return render(text, fontName);
        } catch (Exception e) {
            return "[Error rendering text]";
        }
    }

    private String render(String text, String fontName) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/fonts/" + fontName)) {
            if (in == null) {
                System.err.println("[WARN] Font not found: " + fontName + " (expected at /fonts/" + fontName + ")");
                throw new IOException("Font not found: " + fontName);
            }
            return FigletFont.convertOneLine(in, text);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to render with font '" + fontName + "': " + e.getMessage());
            throw e;
        }
    }

    private static int measureWidth(String ascii) {
        int max = 0;
        for (String line : ascii.split("\\R")) {
            if (line.length() > max)
                max = line.length();
        }
        return max;
    }

    private static String sanitize(String text) {
        return text == null ? "" : text.replaceAll("\\R", " ").trim();
    }

    public record RenderResult(String ascii, String font, int width, boolean fits) {
    }

    // Only allow printable ASCII (32-126)
    private static String filterAscii(String text) {
        return text.replaceAll("[^\\u0020-\\u007E]", "?");
    }

    // Limit input length
    private static String limitLength(String text, int max) {
        if (text.length() > max) {
            return text.substring(0, max);
        }
        return text;
    }

    /**
     * Render multi-line text with a specific font. Each line is rendered separately
     * and joined.
     */
    public String renderMultilineWithFont(String text, String fontName) throws IOException {
        if (text == null)
            return "";
        String[] lines = text.split("\\R");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = sanitize(lines[i]);
            line = filterAscii(line);
            line = limitLength(line, 40);
            try {
                sb.append(render(line, fontName));
            } catch (Exception e) {
                sb.append("[Error rendering line]");
            }
            if (i < lines.length - 1)
                sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Render multi-line text, fitting each line to the maxWidth using available
     * fonts.
     * Returns the result for the first line that fits, or the fallback for the
     * first line.
     * (For simplicity, all lines use the same font.)
     */
    public RenderResult renderMultilineFitting(String text, int maxWidth) throws IOException {
        if (text == null)
            return new RenderResult("", FONT_ORDER.get(0), 0, true);
        String[] lines = text.split("\\R");
        StringBuilder sb = new StringBuilder();
        String usedFont = FONT_ORDER.get(0);
        boolean fits = true;
        int maxLineWidth = 0;
        for (int i = 0; i < lines.length; i++) {
            RenderResult rr = renderFitting(lines[i], maxWidth);
            sb.append(rr.ascii());
            if (i < lines.length - 1)
                sb.append("\n");
            if (!rr.fits())
                fits = false;
            if (rr.width() > maxLineWidth)
                maxLineWidth = rr.width();
            usedFont = rr.font(); // Use the last font used (could be improved)
        }
        return new RenderResult(sb.toString(), usedFont, maxLineWidth, fits);
    }

}