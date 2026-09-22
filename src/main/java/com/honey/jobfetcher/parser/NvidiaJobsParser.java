package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.client.NvidiaJobsClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import com.honey.jobfetcher.provider.JobUrlPolicy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Safely reads NVIDIA's public sitemap and JobPosting JSON-LD metadata.
 */
@Component
public class NvidiaJobsParser {
    private static final int MAX_JSON_LD_DEPTH = 32;
    private static final int MAX_SITEMAP_LOCATIONS = 10_000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<URI> parseSitemap(String response) {
        org.w3c.dom.Document document = SafeXmlParser.parse(response, "NVIDIA sitemap");
        NodeList locations = document.getElementsByTagNameNS("*", "loc");
        if (locations.getLength() > MAX_SITEMAP_LOCATIONS) {
            throw new IllegalStateException("NVIDIA sitemap exceeds the 10,000-location safety limit");
        }

        Set<URI> pages = new LinkedHashSet<>();
        for (int index = 0; index < locations.getLength(); index++) {
            Node location = locations.item(index);
            try {
                URI candidate = URI.create(location.getTextContent().trim());
                URI canonical = NvidiaJobsClient.canonicalPublicJobPage(candidate);
                pages.add(canonical);
            } catch (IllegalArgumentException exception) {
                // Sitemap URLs outside the immutable public-page allowlist are never fetched.
            }
        }
        return new ArrayList<>(pages);
    }

    public Jobs parseJobPage(String response, URI fetchedPage) {
        URI canonicalFetchedPage = NvidiaJobsClient.canonicalPublicJobPage(fetchedPage);
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("NVIDIA job page response must not be blank");
        }

        Document document = Jsoup.parse(response);
        JsonNode jobPosting = findJobPosting(document);
        if (jobPosting == null) {
            throw new IllegalStateException("NVIDIA JobPosting JSON-LD was not found");
        }

        String identifier = value(jobPosting.path("identifier").path("value"));
        String title = value(jobPosting.get("title"));
        if (identifier == null || title == null) {
            throw new IllegalStateException("NVIDIA JobPosting is missing identifier.value or title");
        }

        URI jobUrl = canonicalJobUrl(document, jobPosting, canonicalFetchedPage);
        Jobs job = new Jobs();
        job.setExternalId(JobSource.NVIDIA.name() + ":" + identifier);
        job.setTitle(title);
        job.setDescription(htmlToText(value(jobPosting.get("description"))));
        job.setLocation(jobLocation(jobPosting.get("jobLocation")));
        job.setJobUrl(jobUrl.toString());
        job.setCompany("NVIDIA");
        job.setSource(JobSource.NVIDIA.name());
        job.setStatus(Jobs.STATUS_NO_ACTION);
        return job;
    }

    private JsonNode findJobPosting(Document document) {
        boolean sawJsonLd = false;
        for (Element script : document.select("script")) {
            if (!"application/ld+json".equalsIgnoreCase(script.attr("type").trim())) {
                continue;
            }
            sawJsonLd = true;
            try {
                JsonNode candidate = objectMapper.readTree(script.data());
                JsonNode jobPosting = findJobPosting(candidate, 0);
                if (jobPosting != null) {
                    return jobPosting;
                }
            } catch (JsonProcessingException | RuntimeException exception) {
                throw new IllegalStateException("Unable to parse NVIDIA JobPosting JSON-LD", exception);
            }
        }
        if (!sawJsonLd) {
            return null;
        }
        return null;
    }

    private JsonNode findJobPosting(JsonNode node, int depth) {
        if (node == null || depth > MAX_JSON_LD_DEPTH) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode match = findJobPosting(child, depth + 1);
                if (match != null) {
                    return match;
                }
            }
        } else if (node.isObject()) {
            if (isJobPosting(node.get("@type"))) {
                return node;
            }
            for (JsonNode child : node) {
                JsonNode match = findJobPosting(child, depth + 1);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private boolean isJobPosting(JsonNode type) {
        if (type == null) {
            return false;
        }
        if (type.isTextual()) {
            return "JobPosting".equals(type.asText());
        }
        if (type.isArray()) {
            for (JsonNode value : type) {
                if (isJobPosting(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private URI canonicalJobUrl(Document document, JsonNode jobPosting, URI fetchedPage) {
        String jsonLdUrl = value(jobPosting.get("url"));
        if (jsonLdUrl != null) {
            return canonicalizeNvidiaUrl(jsonLdUrl, fetchedPage);
        }

        Element canonicalLink = document.selectFirst("link[rel=canonical]");
        if (canonicalLink != null && !canonicalLink.attr("href").isBlank()) {
            return canonicalizeNvidiaUrl(canonicalLink.attr("href"), fetchedPage);
        }
        return fetchedPage;
    }

    private URI canonicalizeNvidiaUrl(String value, URI fetchedPage) {
        return JobUrlPolicy.canonicalize(
                value,
                fetchedPage,
                NvidiaJobsClient.NVIDIA_HOST,
                NvidiaJobsClient.JOB_PATH_PREFIXES
        ).orElseThrow(() -> new IllegalStateException("NVIDIA JobPosting URL is outside the public allowlist"));
    }

    private String jobLocation(JsonNode locations) {
        if (locations == null || locations.isNull()) {
            return null;
        }
        JsonNode location = locations.isArray() && !locations.isEmpty() ? locations.get(0) : locations;
        JsonNode address = location == null ? null : location.get("address");
        if (address == null || !address.isObject()) {
            return null;
        }

        String locationText = java.util.stream.Stream.of(
                value(address.get("streetAddress")),
                value(address.get("addressLocality")),
                value(address.get("addressRegion")),
                value(address.get("postalCode")),
                country(address.get("addressCountry"))
        )
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));
        return locationText.isBlank() ? null : locationText;
    }

    private String country(JsonNode country) {
        if (country != null && country.isObject()) {
            return value(country.get("name"));
        }
        return value(country);
    }

    private String value(JsonNode node) {
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isBlank() ? null : value;
    }

    private String htmlToText(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }
}
