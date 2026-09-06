package com.honey.jobfetcher.extractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class PdfDocxTextExtractor implements ResumeTextExtractor {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public String extract(Path file, String contentType) throws IOException {
        if (PDF_CONTENT_TYPE.equals(contentType)) {
            return extractPdf(file);
        }
        if (DOCX_CONTENT_TYPE.equals(contentType)) {
            return extractDocx(file);
        }

        throw new IllegalArgumentException(
                "Unsupported resume content type: " + contentType
        );
    }

    private String extractPdf(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            return new PDFTextStripper().getText(document).trim();
        }
    }

    private String extractDocx(Path file) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();

            document.getParagraphs().forEach(paragraph -> {
                appendLine(text, paragraph.getText());
            });

            document.getTables().forEach(table ->
                    table.getRows().forEach(row ->
                            row.getTableCells().forEach(cell ->
                                    appendLine(text, cell.getText())
                            )
                    )
            );

            return text.toString().trim();
        }
    }

    private void appendLine(StringBuilder text, String value) {
        if (value != null && !value.trim().isEmpty()) {
            if (text.length() > 0) {
                text.append(System.lineSeparator());
            }
            text.append(value.trim());
        }
    }
}
