package com.honey.jobfetcher.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TcsJobsParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("TCS Jobs response must not be blank");
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
                job.setExternalId(JobSource.TCS.name() + ":" + id);
                job.setTitle(title);
                job.setCompany("Tata Consultancy Services");
                job.setLocation(posting.path("location").path("name").asText(null));
                job.setDescription(posting.path("content").asText(null));
                job.setJobUrl(url);
                job.setSource(JobSource.TCS.name());
                job.setStatus(Jobs.STATUS_NO_ACTION);
                jobs.add(job);
            }
            return jobs;
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new IllegalStateException("Unable to parse TCS Greenhouse job data", exception);
        }
    }
}