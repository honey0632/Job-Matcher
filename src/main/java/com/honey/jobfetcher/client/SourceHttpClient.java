package com.honey.jobfetcher.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;

/**
 * Performs bounded, explicit outbound GET requests for approved job providers.
 */
@Component
public class SourceHttpClient {
    private final RestClient restClient;

    public SourceHttpClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, "JobFetcher/1.0")
                .build();
    }

    public String get(URI uri, String sourceName) {
        try {
            String response = restClient.get().uri(uri).retrieve().body(String.class);
            if (response == null || response.isBlank()) {
                throw new IllegalStateException(sourceName + " returned an empty response");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    sourceName + " request failed with status " + exception.getStatusCode().value(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException(sourceName + " request failed", exception);
        }
    }
}
