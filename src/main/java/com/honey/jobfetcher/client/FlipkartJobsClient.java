package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class FlipkartJobsClient {
    private final RestClient restClient;

    public FlipkartJobsClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://www.flipkartcareers.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "JobFetcher/1.0")
                .build();
    }

    public String fetchSearchPage(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/jobs")
                        .queryParam("q", query.trim())
                        .build())
                    .retrieve()
                    .body(String.class);
            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Flipkart Jobs returned an empty response");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "Flipkart Jobs request failed with status " + exception.getStatusCode().value(),
                    exception
            );
        }
    }
}