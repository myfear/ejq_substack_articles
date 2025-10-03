package com.acme.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import com.acme.domain.Invoice;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PdfService {

    public Path render(Invoice invoice) throws IOException {
        final PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream stream = new PDPageContentStream(doc, page);

        // Load fonts
        PDFont titleFont = PDType0Font.load(doc,
                getClass().getClassLoader().getResourceAsStream("IBMPlexSans-Bold.ttf"));
        PDFont regularFont = PDType0Font.load(doc,
                getClass().getClassLoader().getResourceAsStream("IBMPlexSans-Regular.ttf"));

        float yPosition = 750;
        float leftMargin = 50;
        float rightMargin = 550;

        // Header
        stream.beginText();
        stream.setFont(titleFont, 24);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("INVOICE");
        stream.endText();

        yPosition -= 40;

        // Invoice details
        stream.beginText();
        stream.setFont(regularFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Invoice #: " + invoice.id);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Year: " + invoice.year);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Due Date: " + invoice.dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        stream.endText();

        yPosition -= 50;

        // Customer information
        stream.beginText();
        stream.setFont(titleFont, 14);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Bill To:");
        stream.endText();

        yPosition -= 25;
        stream.beginText();
        stream.setFont(regularFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText(invoice.policy.customer.fullName);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText(invoice.policy.customer.street);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText(invoice.policy.customer.postalCode + " " + invoice.policy.customer.city);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText(invoice.policy.customer.countryCode);
        stream.endText();

        yPosition -= 50;

        // Policy information
        stream.beginText();
        stream.setFont(titleFont, 14);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Policy Details:");
        stream.endText();

        yPosition -= 25;
        stream.beginText();
        stream.setFont(regularFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Coverage: " + invoice.policy.coverage);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Region: " + invoice.policy.bundesland);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Vehicle: " + invoice.policy.vehicle.registration + " (" + invoice.policy.vehicle.vin + ")");
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Type Class: " + invoice.policy.vehicle.typklasse);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Regional Class: " + invoice.policy.vehicle.regionalklasse);
        stream.endText();

        yPosition -= 50;

        // Billing table
        stream.beginText();
        stream.setFont(titleFont, 14);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Billing Details:");
        stream.endText();

        yPosition -= 30;

        // Table headers
        stream.beginText();
        stream.setFont(regularFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Description");
        stream.endText();

        stream.beginText();
        stream.newLineAtOffset(rightMargin - 100, yPosition);
        stream.showText("Amount (€)");
        stream.endText();

        yPosition -= 25;

        // Draw line under headers
        stream.moveTo(leftMargin, yPosition + 5);
        stream.lineTo(rightMargin, yPosition + 5);
        stream.stroke();

        yPosition -= 20;

        DecimalFormat df = new DecimalFormat("#,##0.00");

        // Net amount
        stream.beginText();
        stream.setFont(regularFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Annual Premium (Net)");
        stream.endText();

        stream.beginText();
        stream.newLineAtOffset(rightMargin - 100, yPosition);
        stream.showText(df.format(invoice.netAmount));
        stream.endText();

        yPosition -= 20;

        // Tax
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Insurance Tax (19%)");
        stream.endText();

        stream.beginText();
        stream.newLineAtOffset(rightMargin - 100, yPosition);
        stream.showText(df.format(invoice.insuranceTax));
        stream.endText();

        yPosition -= 20;

        // Draw line above total
        stream.moveTo(leftMargin, yPosition + 5);
        stream.lineTo(rightMargin, yPosition + 5);
        stream.stroke();

        yPosition -= 20;

        // Total
        stream.beginText();
        stream.setFont(titleFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("TOTAL");
        stream.endText();

        stream.beginText();
        stream.newLineAtOffset(rightMargin - 100, yPosition);
        stream.showText(df.format(invoice.grossAmount));
        stream.endText();

        yPosition -= 50;

        // Payment information
        stream.beginText();
        stream.setFont(titleFont, 14);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Payment Information:");
        stream.endText();

        yPosition -= 25;
        stream.beginText();
        stream.setFont(regularFont, 12);
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("Frequency: " + invoice.paymentFrequency);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("IBAN: " + invoice.policy.customer.iban);
        stream.endText();

        yPosition -= 20;
        stream.beginText();
        stream.newLineAtOffset(leftMargin, yPosition);
        stream.showText("BIC: " + invoice.policy.customer.bic);
        stream.endText();

        stream.close();

        // Save to file
        String fileName = "invoice_" + invoice.id + "_" + invoice.year + ".pdf";
        Path filePath = Path.of("target/invoices", fileName);
        Files.createDirectories(filePath.getParent());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        Files.write(filePath, baos.toByteArray());
        doc.close();

        return filePath;
    }

}
