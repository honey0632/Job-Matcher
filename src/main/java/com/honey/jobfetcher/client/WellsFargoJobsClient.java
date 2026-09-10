package com.honey.jobfetcher.client;

import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Retrieves Wells Fargo's public XML job feed as one bounded response.
 */
@Component
public class WellsFargoJobsClient {
    static final int MAX_RESPONSE_CHARACTERS = 2_000_000;
    private static final URI JOBS_ENDPOINT = URI.create("https://www.wellsfargojobs.com/en/jobs/xml/");

    private final SourceHttpClient sourceHttpClient;

    public WellsFargoJobsClient(SourceHttpClient sourceHttpClient) {
        this.sourceHttpClient = sourceHttpClient;
    }

    public String fetchAllJobs() {
        String response = sourceHttpClient.get(JOBS_ENDPOINT, "Wells Fargo Jobs");
        if (response.length() > MAX_RESPONSE_CHARACTERS) {
            throw new IllegalStateException("Wells Fargo Jobs response exceeds the 2 MB safety limit");
        }
        return response;
    }
}
