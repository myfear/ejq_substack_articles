package com.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.ColumnDocumentRenderer;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PdfGeneratorService {

    // Font resources
    private static final String REGULAR_FONT_PATH = "IBMPlexSans-Regular.ttf";
    private static final String BOLD_FONT_PATH = "IBMPlexSans-Bold.ttf";
    private static final String LOGO_PATH = "logo.png";
    private static final String FONT_ENCODING = "Identity-H";

    // Page layout constants
    private static final float PAGE_MARGIN = 36f;
    private static final float COLUMN_GAP = 20f;
    private static final float HEADER_HEIGHT = 80f;
    private static final float FOOTER_HEIGHT = 60f;
    
    // Logo dimensions
    private static final float HEADER_LOGO_WIDTH = 150f;
    private static final float HEADER_LOGO_HEIGHT = 50f;
    private static final float FOOTER_LOGO_WIDTH = 100f;
    private static final float FOOTER_LOGO_HEIGHT = 30f;
    
    // Typography
    private static final int H1_FONT_SIZE = 22;
    private static final int H2_FONT_SIZE = 14;
    private static final int BODY_FONT_SIZE = 10;
    private static final int H2_MARGIN_TOP = 15;
    private static final int PARAGRAPH_INDENT = 12;

    // Markdown prefixes
    private static final String H1_PREFIX = "# ";
    private static final String H2_PREFIX = "## ";
    private static final String LIST_PREFIX = "* ";

    public byte[] createWhitePaperPdf(String content) throws Exception {
        validateContent(content);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            FontHolder fonts = loadFonts();
            Image logoImage = loadLogo();
            
            setupDocument(document);
            processContent(document, content, fonts);
            addFooterLogos(pdfDoc, logoImage);
            
            document.close();
            return baos.toByteArray();
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
    }

    private FontHolder loadFonts() throws IOException {
        try {
            PdfFont regularFont = createFontFromResource(REGULAR_FONT_PATH);
            PdfFont boldFont = createFontFromResource(BOLD_FONT_PATH);
            return new FontHolder(regularFont, boldFont);
        } catch (Exception e) {
            throw new IOException("Failed to load fonts", e);
        }
    }

    private PdfFont createFontFromResource(String fontPath) throws IOException {
        URL fontResource = getClass().getClassLoader().getResource(fontPath);
        if (fontResource == null) {
            throw new IOException("Font resource not found: " + fontPath);
        }
        
        try (InputStream fontStream = fontResource.openStream()) {
            return PdfFontFactory.createFont(fontStream.readAllBytes(), FONT_ENCODING);
        }
    }

    private Image loadLogo() {
        URL logoResource = getClass().getClassLoader().getResource(LOGO_PATH);
        if (logoResource == null) {
            return null;
        }
        
        try {
            Image logoImage = new Image(ImageDataFactory.create(logoResource));
            logoImage.scaleToFit(HEADER_LOGO_WIDTH, HEADER_LOGO_HEIGHT);
            logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            return logoImage;
        } catch (Exception e) {
            // Log warning and continue without logo
            return null;
        }
    }

    private void setupDocument(Document document) {
        Rectangle[] columns = createColumnLayout();
        document.setRenderer(new ColumnDocumentRenderer(document, columns));
    }

    private Rectangle[] createColumnLayout() {
        float availableWidth = PageSize.A4.getWidth() - (2 * PAGE_MARGIN);
        float columnWidth = (availableWidth - COLUMN_GAP) / 2;
        float contentHeight = PageSize.A4.getHeight() - HEADER_HEIGHT - FOOTER_HEIGHT;
        
        return new Rectangle[]{
            new Rectangle(PAGE_MARGIN, HEADER_HEIGHT, columnWidth, contentHeight),
            new Rectangle(PAGE_MARGIN + columnWidth + COLUMN_GAP, HEADER_HEIGHT, columnWidth, contentHeight)
        };
    }

    private void processContent(Document document, String content, FontHolder fonts) {
        String[] lines = content.split("\n");
        List currentList = null;

        for (String line : lines) {
            currentList = processLine(document, line, fonts, currentList);
        }

        // Add any remaining list
        if (currentList != null) {
            document.add(currentList);
        }
    }

    private List processLine(Document document, String line, FontHolder fonts, List currentList) {
        if (line.startsWith(H1_PREFIX)) {
            currentList = addPendingList(document, currentList);
            addHeading1(document, line, fonts);
        } else if (line.startsWith(H2_PREFIX)) {
            currentList = addPendingList(document, currentList);
            addHeading2(document, line, fonts);
        } else if (line.startsWith(LIST_PREFIX)) {
            currentList = addListItem(currentList, line, fonts);
        } else if (!line.trim().isEmpty()) {
            currentList = addPendingList(document, currentList);
            addParagraph(document, line, fonts);
        }
        return currentList;
    }

    private List addPendingList(Document document, List currentList) {
        if (currentList != null) {
            document.add(currentList);
        }
        return null;
    }

    private void addHeading1(Document document, String line, FontHolder fonts) {
        Paragraph heading = new Paragraph(line.substring(H1_PREFIX.length()))
            .setFont(fonts.bold)
            .setFontSize(H1_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(heading);
    }

    private void addHeading2(Document document, String line, FontHolder fonts) {
        Paragraph heading = new Paragraph(line.substring(H2_PREFIX.length()))
            .setFont(fonts.bold)
            .setFontSize(H2_FONT_SIZE)
            .setMarginTop(H2_MARGIN_TOP);
        document.add(heading);
    }

    private List addListItem(List currentList, String line, FontHolder fonts) {
        if (currentList == null) {
            currentList = new List();
            currentList.setFont(fonts.regular).setFontSize(BODY_FONT_SIZE);
        }
        
        ListItem item = new ListItem();
        item.add(new Paragraph(line.substring(LIST_PREFIX.length())));
        item.setTextAlignment(TextAlignment.JUSTIFIED);
        currentList.add(item);
        return currentList;
    }

    private void addParagraph(Document document, String line, FontHolder fonts) {
        Paragraph paragraph = new Paragraph(line)
            .setFont(fonts.regular)
            .setFontSize(BODY_FONT_SIZE)
            .setTextAlignment(TextAlignment.JUSTIFIED)
            .setFirstLineIndent(PARAGRAPH_INDENT);
        document.add(paragraph);
    }

    private void addFooterLogos(PdfDocument pdfDoc, Image logoImage) {
        if (logoImage == null) {
            return;
        }

        try {
            URL logoResource = getClass().getClassLoader().getResource(LOGO_PATH);
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                addFooterLogo(pdfDoc.getPage(i), logoResource);
            }
        } catch (Exception e) {
            // Log warning and continue without footer logos
        }
    }

    private void addFooterLogo(PdfPage page, URL logoResource) {
        try {
            PdfCanvas canvas = new PdfCanvas(page);
            Rectangle footerRect = new Rectangle(
                PAGE_MARGIN, 
                20, 
                PageSize.A4.getWidth() - (2 * PAGE_MARGIN), 
                40
            );
            
            try (Canvas footerCanvas = new Canvas(canvas, footerRect)) {
                Image footerLogo = new Image(ImageDataFactory.create(logoResource));
                footerLogo.scaleToFit(FOOTER_LOGO_WIDTH, FOOTER_LOGO_HEIGHT);
                footerLogo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                footerCanvas.add(footerLogo);
            }
        } catch (Exception e) {
            // Log warning and continue
        }
    }

    private static class FontHolder {
        private final PdfFont regular;
        private final PdfFont bold;

        public FontHolder(PdfFont regular, PdfFont bold) {
            this.regular = regular;
            this.bold = bold;
        }
    }
}