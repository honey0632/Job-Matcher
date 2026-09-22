package com.honey.jobfetcher.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabricksJobsParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Databricks Jobs response must not be blank");
        }

        try {
            List<Jobs> jobs = new ArrayList<>();
            JsonNode root = objectMapper.readTree(response);
            JsonNode postings = root.path("jobs");
            if (postings.isArray()) {
                for (JsonNode posting : postings) {
                    String id = posting.path("id").asText(null);
                    String title = posting.path("title").asText(null);
                    String url = posting.path("absolute_url").asText(null);
                    if (id == null || title == null || url == null) {
                        continue;
                    }

                    Jobs job = new Jobs();
                    job.setExternalId(JobSource.DATABRICKS.name() + ":" + id);
                    job.setTitle(title);
                    job.setCompany("Databricks");
                    job.setLocation(posting.path("location").path("name").asText(null));
                    job.setDescription(posting.path("content").asText(null));
                    job.setJobUrl(url);
                    job.setSource(JobSource.DATABRICKS.name());
                    job.setStatus(Jobs.STATUS_NO_ACTION);
                    jobs.add(job);
                }
            }
            return jobs;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to parse Databricks job data", exception);
        }
    }
}