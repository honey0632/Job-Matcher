// Parses Google Careers responses into application job listings.

package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleCareersParser {

    private static final String CALLBACK_MARKER = "AF_initDataCallback";
    private static final String JOB_CALLBACK_KEY = "key: 'ds:1'";
    private static final String CAREERS_DATA_MARKER = "projects/gweb-careers-proto";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Jobs> parse(String html) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("Google Careers response must not be blank");
        }

        String callback = findJobCallback(html);
        String dataJson = extractDataArray(callback);

        try {
            JsonNode root = objectMapper.readTree(dataJson);
            return readJobs(root);
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new IllegalStateException("Unable to parse Google Careers job data", exception);
        }
    }

    private String findJobCallback(String html) {
        Document document = Jsoup.parse(html);

        for (Element script : document.select("script")) {
            String scriptText = script.data();

            if (scriptText.contains(CALLBACK_MARKER)
                    && scriptText.contains(JOB_CALLBACK_KEY)
                    && scriptText.contains(CAREERS_DATA_MARKER)) {
                return scriptText;
            }
        }

        throw new IllegalStateException("Google Careers job data callback was not found");
    }

    private String extractDataArray(String callback) {
        int dataStart = callback.indexOf("data:");
        if (dataStart < 0) {
            throw new IllegalStateException("Google Careers callback does not contain data");
        }

        int arrayStart = callback.indexOf('[', dataStart);
        if (arrayStart < 0) {
            throw new IllegalStateException("Google Careers data array was not found");
        }

        int arrayEnd = findMatchingBracket(callback, arrayStart);
        return callback.substring(arrayStart, arrayEnd + 1);
    }

    private int findMatchingBracket(String value, int start) {
        int depth = 0;
        boolean insideString = false;
        boolean escaped = false;

        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);

            if (insideString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    insideString = false;
                }
                continue;
            }

            if (current == '"') {
                insideString = true;
            } else if (current == '[') {
                depth++;
            } else if (current == ']' && --depth == 0) {
                return index;
            }
        }

        throw new IllegalStateException("Google Careers data array is incomplete");
    }

    private List<Jobs> readJobs(JsonNode root) {
        List<Jobs> jobs = new ArrayList<>();
        JsonNode records = locateJobRecords(root);

        if (records == null || !records.isArray()) {
            throw new IllegalStateException("Google Careers job records were not found");
        }

        for (JsonNode record : records) {
            Jobs job = toJob(record);
            if (job != null) {
                jobs.add(job);
            }
        }

        return jobs;
    }

    private JsonNode locateJobRecords(JsonNode root) {
        if (!root.isArray()) {
            return null;
        }

        for (JsonNode node : root) {
            if (node.isArray() && !node.isEmpty() && looksLikeJobRecord(node.get(0))) {
                return node;
            }

            JsonNode nested = locateJobRecords(node);
            if (nested != null) {
                return nested;
            }
        }

        return null;
    }

    private boolean looksLikeJobRecord(JsonNode node) {
        return node != null
                && node.isArray()
                && node.size() >= 3
                && node.get(0).isTextual()
                && node.get(1).isTextual()
                && node.get(2).isTextual()
                && node.get(2).asText().contains("google.com");
    }

    private Jobs toJob(JsonNode record) {
        if (!looksLikeJobRecord(record)) {
            return null;
        }

        String externalId = text(record, 0);
        String title = text(record, 1);
        String jobUrl = text(record, 2);
        String company = text(record, 7);
        String location = firstLocation(record.get(9));
        String description = combineHtmlFields(record, 3, 4, 10);

        if (externalId == null || title == null || jobUrl == null) {
            return null;
        }

        Jobs job = new Jobs();
        job.setExternalId(externalId);
        job.setTitle(title);
        job.setJobUrl(jobUrl);
        job.setCompany(company == null ? "Google" : company);
        job.setLocation(location);
        job.setDescription(description);
        job.setSource("GOOGLE_CAREERS");
        job.setStatus(Jobs.STATUS_NO_ACTION);
        return job;
    }

    private String text(JsonNode node, int index) {
        if (node == null || node.size() <= index || !node.get(index).isTextual()) {
            return null;
        }
        return node.get(index).asText();
    }

    private String firstLocation(JsonNode locations) {
        if (locations == null || !locations.isArray() || locations.isEmpty()) {
            return null;
        }

        JsonNode firstLocation = locations.get(0);
        return firstLocation.isArray() && !firstLocation.isEmpty()
                ? text(firstLocation, 0)
                : null;
    }

    private String combineHtmlFields(JsonNode record, int... indexes) {
        List<String> sections = new ArrayList<>();

        for (int index : indexes) {
            JsonNode field = record.get(index);
            if (field != null && field.isArray() && field.size() > 1 && field.get(1).isTextual()) {
                String text = Jsoup.parse(field.get(1).asText()).text();
                if (!text.isBlank()) {
                    sections.add(text);
                }
            }
        }

        return String.join("\n\n", sections);
    }
}
