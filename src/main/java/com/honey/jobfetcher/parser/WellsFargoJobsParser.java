package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobQuery;
import com.honey.jobfetcher.provider.JobSource;
import com.honey.jobfetcher.provider.JobUrlPolicy;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Converts Wells Fargo's public XML feed while rejecting unsafe XML constructs.
 */
@Component
public class WellsFargoJobsParser {
    static final int MAX_JOB_RECORDS = 500;
    private static final URI WELLS_FARGO_BASE_URI = URI.create("https://www.wellsfargojobs.com");
    private static final String WELLS_FARGO_HOST = "www.wellsfargojobs.com";

    public List<Jobs> parse(String response, String query) {
        JobQuery.requireValid(query);
        Document document = SafeXmlParser.parse(response, "Wells Fargo Jobs");
        NodeList jobNodes = document.getElementsByTagNameNS("*", "job");
        if (jobNodes.getLength() > MAX_JOB_RECORDS) {
            throw new IllegalStateException("Wells Fargo Jobs response exceeds the 500-record safety limit");
        }

        List<Jobs> jobs = new ArrayList<>();
        for (int index = 0; index < jobNodes.getLength(); index++) {
            if (!(jobNodes.item(index) instanceof Element jobElement)) {
                continue;
            }
            toJob(jobElement, query).ifPresent(jobs::add);
        }
        return jobs;
    }

    private Optional<Jobs> toJob(Element jobElement, String query) {
        String referenceNumber = childText(jobElement, "referencenumber");
        String title = childText(jobElement, "title");
        String description = htmlToText(childText(jobElement, "description"));
        String jobUrl = firstNonBlank(
                childText(jobElement, "url"),
                childText(jobElement, "joburl"),
                childText(jobElement, "applyurl")
        );
        URI canonicalUrl = JobUrlPolicy.canonicalize(
                jobUrl,
                WELLS_FARGO_BASE_URI,
                WELLS_FARGO_HOST,
                "/en/jobs/"
        ).orElse(null);

        if (referenceNumber == null || title == null || canonicalUrl == null
                || !JobQuery.matchesText(query, title, description)) {
            return Optional.empty();
        }

        Jobs job = new Jobs();
        job.setExternalId(JobSource.WELLS_FARGO.name() + ":" + referenceNumber);
        job.setTitle(title);
        job.setDescription(description);
        job.setLocation(location(jobElement));
        job.setJobUrl(canonicalUrl.toString());
        job.setCompany("Wells Fargo");
        job.setSource(JobSource.WELLS_FARGO.name());
        job.setStatus(Jobs.STATUS_NO_ACTION);
        return Optional.of(job);
    }

    private String location(Element jobElement) {
        String location = java.util.stream.Stream.of(
                childText(jobElement, "city"),
                childText(jobElement, "state"),
                childText(jobElement, "country"),
                childText(jobElement, "postalcode")
        )
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));
        return location.isBlank() ? null : location;
    }

    private String childText(Element element, String fieldName) {
        NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            String childName = child.getLocalName() == null ? child.getNodeName() : child.getLocalName();
            if (fieldName.equalsIgnoreCase(childName)) {
                String value = child.getTextContent();
                if (value == null) {
                    return null;
                }
                value = value.trim();
                return value.isBlank() ? null : value;
            }
        }
        return null;
    }

    private String htmlToText(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
