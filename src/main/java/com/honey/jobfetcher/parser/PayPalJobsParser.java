package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class PayPalJobsParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("PayPal Jobs response must not be blank");
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
                    job.setExternalId(JobSource.PAYPAL.name() + ":" + id);
                    job.setTitle(title);
                    job.setCompany("PayPal");
                    job.setLocation(posting.path("location").path("name").asText(null));
                    job.setDescription(posting.path("content").asText(null));
                    job.setJobUrl(url);
                    job.setSource(JobSource.PAYPAL.name());
                    job.setStatus(Jobs.STATUS_NO_ACTION);
                    jobs.add(job);
                }
            }
            return jobs;
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new IllegalStateException("Unable to parse PayPal job data", exception);
        }
    }
}