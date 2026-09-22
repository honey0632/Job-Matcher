package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class PayPalJobsClient {
    private final RestClient restClient;

    public PayPalJobsClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://www.paypal.com")
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
                        .path("/us/about/jobs/search-results")
                        .queryParam("q", query.trim())
                        .build())
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("PayPal Jobs returned an empty response");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "PayPal Jobs request failed with status " + exception.getStatusCode().value(),
                    exception
            );
        }
    }
}