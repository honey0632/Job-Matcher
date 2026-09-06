package com.honey.jobfetcher.extractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfDocxTextExtractorTest {

    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final PdfDocxTextExtractor extractor = new PdfDocxTextExtractor();

    @Test
    void extractsTextFromPdf(@TempDir Path directory) throws Exception {
        Path pdf = directory.resolve("resume.pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Software Engineer");
                content.endText();
            }
            document.save(pdf.toFile());
        }

        String text = extractor.extract(pdf, "application/pdf");

        assertTrue(text.contains("Software Engineer"));
    }

    @Test
    void extractsTextFromDocx(@TempDir Path directory) throws Exception {
        Path docx = directory.resolve("resume.docx");

        try (XWPFDocument document = new XWPFDocument();
             OutputStream output = Files.newOutputStream(docx)) {
            document.createParagraph().createRun().setText("Backend Developer");
            document.write(output);
        }

        String text = extractor.extract(docx, DOCX_CONTENT_TYPE);

        assertTrue(text.contains("Backend Developer"));
    }
}
