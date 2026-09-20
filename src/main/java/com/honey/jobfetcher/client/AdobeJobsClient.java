package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AdobeJobsClient {
    private final RestClient restClient;

    public AdobeJobsClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://adobe.wd5.myworkdayjobs.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "JobFetcher/1.0")
                .build();
    }

    public String fetchSearchPage(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        try {
            String response = restClient.post()
                    .uri("/wday/cxs/adobe/external_experienced/jobs")
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("""
                            {"appliedFacets":{},"limit":100,"offset":0,"searchText":"%s"}
                            """.formatted(escapeJson(query.trim())))
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Adobe Jobs returned an empty response");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "Adobe Jobs request failed with status "
                            + exception.getStatusCode().value(),
                    exception
            );
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}