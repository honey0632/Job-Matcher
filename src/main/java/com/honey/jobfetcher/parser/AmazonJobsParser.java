package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import com.honey.jobfetcher.provider.JobUrlPolicy;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts the documented Amazon Jobs JSON search response into job listings.
 */
@Component
public class AmazonJobsParser {
    private static final URI AMAZON_BASE_URI = URI.create("https://www.amazon.jobs");
    private static final String AMAZON_HOST = "www.amazon.jobs";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Amazon Jobs response must not be blank");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to parse Amazon Jobs JSON response", exception);
        }

        JsonNode records = root == null ? null : root.get("jobs");
        if (root == null || !root.isObject() || records == null || !records.isArray()) {
            throw new IllegalStateException("Amazon Jobs response does not contain a jobs array");
        }

        List<Jobs> jobs = new ArrayList<>();
        for (JsonNode record : records) {
            if (!record.isObject()) {
                continue;
            }
            toJob(record).ifPresent(jobs::add);
        }
        return jobs;
    }

    private java.util.Optional<Jobs> toJob(JsonNode record) {
        String id = value(record.get("id_icims"));
        String title = value(record.get("title"));
        String jobPath = value(record.get("job_path"));
        URI jobUrl = JobUrlPolicy.canonicalize(jobPath, AMAZON_BASE_URI, AMAZON_HOST, "/en/")
                .orElse(null);
        if (id == null || title == null || jobUrl == null) {
            return java.util.Optional.empty();
        }

        Jobs job = new Jobs();
        job.setExternalId(JobSource.AMAZON.name() + ":" + id);
        job.setTitle(title);
        job.setLocation(firstNonBlank(value(record.get("normalized_location")), value(record.get("location"))));
        job.setDescription(htmlToText(value(record.get("description"))));
        job.setJobUrl(jobUrl.toString());
        job.setCompany("Amazon");
        job.setSource(JobSource.AMAZON.name());
        job.setStatus(Jobs.STATUS_NO_ACTION);
        return java.util.Optional.of(job);
    }

    private String value(JsonNode node) {
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isBlank() ? null : value;
    }

    private String firstNonBlank(String first, String second) {
        return first == null ? second : first;
    }

    private String htmlToText(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }
}
