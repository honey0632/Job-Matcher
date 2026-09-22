package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class StripeJobsParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Stripe Jobs response must not be blank");
        }

        try {
            List<Jobs> jobs = new ArrayList<>();
            for (JsonNode posting : objectMapper.readTree(response).path("jobs")) {
                String id = posting.path("id").asText(null);
                String title = posting.path("title").asText(null);
                String url = posting.path("absolute_url").asText(null);
                if (id == null || title == null || url == null) {
                    continue;
                }

                Jobs job = new Jobs();
                job.setExternalId(JobSource.STRIPE.name() + ":" + id);
                job.setTitle(title);
                job.setCompany("Stripe");
                job.setLocation(posting.path("location").path("name").asText(null));
                job.setDescription(posting.path("content").asText(null));
                job.setJobUrl(url);
                job.setSource(JobSource.STRIPE.name());
                job.setStatus(Jobs.STATUS_NO_ACTION);
                jobs.add(job);
            }
            return jobs;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to parse Stripe Greenhouse job data", exception);
        }
    }
}