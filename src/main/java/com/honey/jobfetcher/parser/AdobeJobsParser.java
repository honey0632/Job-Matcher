package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdobeJobsParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Adobe Jobs response must not be blank");
        }

        try {
            List<Jobs> jobs = new ArrayList<>();
            for (JsonNode posting : objectMapper.readTree(response).path("jobPostings")) {
                String title = text(posting, "title");
                String path = text(posting, "externalPath");
                if (title == null || path == null) {
                    continue;
                }

                Jobs job = new Jobs();
                job.setExternalId(JobSource.ADOBE.name() + ":" + path);
                job.setTitle(title);
                job.setCompany("Adobe");
                job.setLocation(text(posting, "locationsText"));
                job.setDescription(text(posting, "jobDescription"));
                job.setJobUrl("https://adobe.wd5.myworkdayjobs.com" + path);
                job.setSource(JobSource.ADOBE.name());
                job.setStatus(Jobs.STATUS_NO_ACTION);
                jobs.add(job);
            }
            return jobs;
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new IllegalStateException("Unable to parse Adobe Workday job data", exception);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }
}
