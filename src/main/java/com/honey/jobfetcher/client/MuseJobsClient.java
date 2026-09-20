package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MuseJobsClient {
    private final RestClient restClient;

    public MuseJobsClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://www.themuse.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "JobFetcher/1.0")
                .build();
    }

    public String fetchJobs(String company, String query) {
        if (company == null || company.isBlank() || query == null || query.isBlank()) {
            throw new IllegalArgumentException("Company and search query must not be blank");
        }

        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/public/jobs")
                            .queryParam("company", company)
                            .queryParam("page", 0)
                            .build())
                    .retrieve()
                    .body(String.class);
            if (response == null || response.isBlank()) {
                throw new IllegalStateException("The Muse returned an empty response");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "The Muse request failed with status " + exception.getStatusCode().value(),
                    exception
            );
        }
    }
}
