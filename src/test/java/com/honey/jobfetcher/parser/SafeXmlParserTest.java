// Verifies the secure XML parsing boundary independently of provider-specific parsers.

package com.honey.jobfetcher.parser;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SafeXmlParserTest {

    @Test
    void parsesNamespacedXmlDocument() {
        Document document = SafeXmlParser.parse("""
                <jobs xmlns="https://example.com/jobs"><job id="42">Engineer</job></jobs>
                """, "Example source");

        assertEquals("jobs", document.getDocumentElement().getLocalName());
        assertEquals("42", document.getDocumentElement().getFirstChild().getAttributes()
                .getNamedItem("id").getNodeValue());
    }

    @Test
    void rejectsEmptyXmlWithSourceName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SafeXmlParser.parse("   ", "Example source"));

        assertEquals("Example source returned an empty XML response", exception.getMessage());
    }

    @Test
    void rejectsMalformedXmlWithSourceName() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> SafeXmlParser.parse("<jobs><job></jobs>", "Example source"));

        assertEquals("Unable to parse Example source XML response", exception.getMessage());
    }

    @Test
    void rejectsDtdBeforeExternalEntityResolution() {
        String unsafeXml = """
                <!DOCTYPE jobs [<!ENTITY sensitive SYSTEM "file:///not-allowed">]>
                <jobs><job>&sensitive;</job></jobs>
                """;

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> SafeXmlParser.parse(unsafeXml, "Example source"));

        assertEquals("Unable to parse Example source XML response", exception.getMessage());
    }
}
