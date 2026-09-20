package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;

@Component
public class SourceHttpClient {

    private final RestClient restClient;

    public SourceHttpClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String get(URI uri, String sourceName) {
        try {
            return restClient.get()
                .uri(uri)
                .header(HttpHeaders.USER_AGENT, "JobFetcher/1.0")
                .retrieve()
                .body(String.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                sourceName + " request failed with status " + exception.getStatusCode().value(),
                exception
            );
        }
    }
}