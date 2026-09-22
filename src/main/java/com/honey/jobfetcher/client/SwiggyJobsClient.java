package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class SwiggyJobsClient {
    private final RestClient restClient;

    public SwiggyJobsClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://careers.swiggy.com")
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
                throw new IllegalStateException("Swiggy Jobs returned an empty response");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "Swiggy Jobs request failed with status " + exception.getStatusCode().value(),
                    exception
            );
        }
    }
}