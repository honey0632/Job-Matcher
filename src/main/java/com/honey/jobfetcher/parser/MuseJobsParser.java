package com.honey.jobfetcher.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class MuseJobsParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String response, JobSource source, String query) {
        try {
            List<Jobs> jobs = new ArrayList<>();
            for (JsonNode posting : objectMapper.readTree(response).path("results")) {
                String id = posting.path("id").asText(null);
                String title = posting.path("name").asText(null);
                String url = posting.path("refs").path("landing_page").asText(null);
                if (id == null || title == null || url == null) {
                    continue;
                }
                String contents = Jsoup.parse(posting.path("contents").asText("")).text();
                String searchable = (title + " " + contents).toLowerCase();
                if (!matchesQuery(searchable, query)) {
                    continue;
                }

                Jobs job = new Jobs();
                job.setExternalId(source.name() + ":" + id);
                job.setTitle(title);
                job.setCompany(posting.path("company").path("name").asText(source.name()));
                job.setLocation(firstLocation(posting.path("locations")));
                job.setDescription(contents);
                job.setJobUrl(url);
                job.setSource(source.name());
                job.setStatus(Jobs.STATUS_NO_ACTION);
                jobs.add(job);
            }
            return jobs;
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new IllegalStateException("Unable to parse The Muse job data", exception);
        }
    }

    private String firstLocation(JsonNode locations) {
        if (!locations.isArray() || locations.isEmpty()) {
            return null;
        }
        return locations.get(0).path("name").asText(null);
    }

    private boolean matchesQuery(String searchable, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return java.util.Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(token -> token.length() > 2)
                .anyMatch(searchable::contains);
    }
}
