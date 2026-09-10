package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmazonJobsParserTest {
    private final AmazonJobsParser parser = new AmazonJobsParser();

    @Test
    void mapsAmazonJobsAndCanonicalizesRelativeJobPaths() {
        List<Jobs> jobs = parser.parse("""
                {"jobs":[
                  {
                    "id_icims":"12345",
                    "title":"Software Development Engineer",
                    "normalized_location":"Bengaluru, KA, IND",
                    "location":"India",
                    "description":"<p>Build <strong>reliable</strong> services.</p>",
                    "job_path":"/en/jobs/12345/software-development-engineer"
                  }
                ]}
                """);

        assertEquals(1, jobs.size());
        Jobs job = jobs.getFirst();
        assertEquals("AMAZON:12345", job.getExternalId());
        assertEquals("Bengaluru, KA, IND", job.getLocation());
        assertEquals("Build reliable services.", job.getDescription());
        assertEquals("https://www.amazon.jobs/en/jobs/12345/software-development-engineer", job.getJobUrl());
        assertEquals("Amazon", job.getCompany());
        assertEquals("AMAZON", job.getSource());
    }

    @Test
    void skipsRecordsWithUrlsOutsideAmazonJobs() {
        List<Jobs> jobs = parser.parse("""
                {"jobs":[{"id_icims":"12345","title":"Engineer",
                "job_path":"https://example.test/en/jobs/12345","description":"text"}]}
                """);

        assertEquals(List.of(), jobs);
    }

    @Test
    void rejectsMalformedResponseShapes() {
        assertThrows(IllegalStateException.class, () -> parser.parse("{\"results\":[]}"));
    }
}
