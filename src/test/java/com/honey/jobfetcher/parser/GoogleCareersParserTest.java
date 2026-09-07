// Verifies conversion of Google Careers data into job listings.

package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleCareersParserTest {

    private final GoogleCareersParser parser = new GoogleCareersParser();

    @Test
    void parsesGoogleCareersJobRecords() {
        String response = """
                <html><script>
                AF_initDataCallback({key: 'ds:1', data:[[[
                "job-1",
                "Senior Software Engineer",
                "https://www.google.com/about/careers/applications/signin?jobId=job-1",
                [null,"<ul><li>Build reliable services.</li></ul>"],
                [null,"<h3>Minimum qualifications:</h3><p>Java experience.</p>"],
                "projects/gweb-careers-proto/companies/google",
                null,
                "Google",
                "en-US",
                [["Bengaluru, India",["Bengaluru"]]],
                [null,"<p>Work on important products.</p>"]
                ]],null,1,1]});
                </script></html>
                """;

        List<Jobs> jobs = parser.parse(response);

        assertEquals(1, jobs.size());
        assertEquals("job-1", jobs.get(0).getExternalId());
        assertEquals("Senior Software Engineer", jobs.get(0).getTitle());
        assertEquals("Google", jobs.get(0).getCompany());
        assertEquals("Bengaluru, India", jobs.get(0).getLocation());
        assertEquals(
                "Build reliable services.\n\nMinimum qualifications: Java experience.\n\nWork on important products.",
                jobs.get(0).getDescription()
        );
        assertEquals("GOOGLE_CAREERS", jobs.get(0).getSource());
        assertEquals(Jobs.STATUS_NO_ACTION, jobs.get(0).getStatus());
    }

    @Test
    void rejectsResponseWithoutJobCallback() {
        assertThrows(
                IllegalStateException.class,
                () -> parser.parse("<html><body>No jobs</body></html>")
        );
    }
}
