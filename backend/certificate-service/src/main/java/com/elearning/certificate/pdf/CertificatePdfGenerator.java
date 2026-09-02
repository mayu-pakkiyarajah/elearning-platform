package com.elearning.certificate.pdf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

/**
 * Draws a simple, landscape, single-page completion certificate. No external
 * template engine or design tool involved — just PDFBox primitives (lines, text,
 * centered strings) plus a generated QR code image. Good enough for a portfolio
 * project; a real product would likely use a proper template (e.g. an HTML->PDF
 * renderer) for easier restyling.
 */
@Component
public class CertificatePdfGenerator {

    private static final float PAGE_WIDTH = PDRectangle.A4.getHeight();  // landscape
    private static final float PAGE_HEIGHT = PDRectangle.A4.getWidth();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final int QR_SIZE_PX = 300;   // rendered resolution; drawn much smaller on the page
    private static final float QR_DRAW_SIZE = 70f;

    public byte[] generate(String studentName, String courseTitle, LocalDate issuedDate,
                            String verificationCode, String verificationUrl) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            document.addPage(page);

            PDImageXObject qrImage = PDImageXObject.createFromByteArray(
                    document, generateQrCodePng(verificationUrl), "verification-qr");

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawBorder(content);
                drawCenteredText(content, PDType1Font.HELVETICA_BOLD, 28,
                        PAGE_HEIGHT - 140, "Certificate of Completion");

                drawCenteredText(content, PDType1Font.HELVETICA, 14,
                        PAGE_HEIGHT - 190, "This certifies that");

                drawCenteredText(content, PDType1Font.HELVETICA_BOLD, 24,
                        PAGE_HEIGHT - 230, studentName);

                drawCenteredText(content, PDType1Font.HELVETICA, 14,
                        PAGE_HEIGHT - 270, "has successfully completed the course");

                drawCenteredText(content, PDType1Font.HELVETICA_BOLD, 20,
                        PAGE_HEIGHT - 310, courseTitle);

                drawCenteredText(content, PDType1Font.HELVETICA, 12,
                        PAGE_HEIGHT - 360, "Issued on " + issuedDate.format(DATE_FORMAT));

                // QR code bottom-right — scan to verify, doesn't compete with the
                // centered text block above it
                float qrX = PAGE_WIDTH - 110f;
                float qrY = 50f;
                content.drawImage(qrImage, qrX, qrY, QR_DRAW_SIZE, QR_DRAW_SIZE);

                drawCenteredText(content, PDType1Font.HELVETICA, 10,
                        60, "Verification code: " + verificationCode);

                drawCenteredText(content, PDType1Font.HELVETICA, 9,
                        44, "Scan the QR code or visit " + verificationUrl + " to verify this certificate");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            // PDFBox failures here mean something is structurally wrong (bad font,
            // corrupted document) — not a recoverable per-request condition, so this
            // surfaces as a 500 rather than a typed business exception.
            throw new UncheckedIOException("Failed to generate certificate PDF", e);
        }
    }

    private byte[] generateQrCodePng(String content) throws IOException {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return out.toByteArray();
        } catch (WriterException e) {
            // Only happens for malformed input to the encoder (e.g. an empty string) —
            // the verification URL is always well-formed here, so this is effectively
            // unreachable, but IOException is what the caller already handles.
            throw new IOException("Failed to generate QR code", e);
        }
    }

    private void drawBorder(PDPageContentStream content) throws IOException {
        float margin = 24f;
        content.setLineWidth(3f);
        content.addRect(margin, margin, PAGE_WIDTH - 2 * margin, PAGE_HEIGHT - 2 * margin);
        content.stroke();
    }

    private void drawCenteredText(PDPageContentStream content, PDFont font, float fontSize, float y, String text) throws IOException {
        float width = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PAGE_WIDTH - width) / 2;

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }
}
