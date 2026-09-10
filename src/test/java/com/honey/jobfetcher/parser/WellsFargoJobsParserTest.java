package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WellsFargoJobsParserTest {
    private final WellsFargoJobsParser parser = new WellsFargoJobsParser();

    @Test
    void mapsAndLocallyFiltersPublicXmlJobs() {
        List<Jobs> jobs = parser.parse("""
                <jobs>
                  <job>
                    <referencenumber>R-123</referencenumber>
                    <title>Backend Engineer</title>
                    <city>Bengaluru</city><state>Karnataka</state>
                    <country>India</country><postalcode>560001</postalcode>
                    <description><![CDATA[<p>Build <b>Java</b> services.</p>]]></description>
                    <url>/en/jobs/R-123/backend-engineer</url>
                  </job>
                  <job>
                    <referencenumber>R-456</referencenumber><title>Recruiter</title>
                    <description>People operations</description>
                    <url>/en/jobs/R-456/recruiter</url>
                  </job>
                </jobs>
                """, "Backend Engineer India 3 years experience");

        assertEquals(1, jobs.size());
        Jobs job = jobs.getFirst();
        assertEquals("WELLS_FARGO:R-123", job.getExternalId());
        assertEquals("Build Java services.", job.getDescription());
        assertEquals("Bengaluru, Karnataka, India, 560001", job.getLocation());
        assertEquals("https://www.wellsfargojobs.com/en/jobs/R-123/backend-engineer", job.getJobUrl());
        assertEquals("Wells Fargo", job.getCompany());
    }

    @Test
    void rejectsDtdsBeforeExternalEntitiesCanResolve() {
        String unsafeXml = """
                <!DOCTYPE jobs [<!ENTITY sensitive SYSTEM "file:///not-allowed">]>
                <jobs><job><referencenumber>&sensitive;</referencenumber></job></jobs>
                """;

        assertThrows(IllegalStateException.class, () -> parser.parse(unsafeXml, "Engineer"));
    }
}
