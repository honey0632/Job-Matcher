package com.honey.jobfetcher.client;

import com.honey.jobfetcher.provider.JobQuery;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Retrieves paginated public Amazon Jobs search results.
 */
@Component
public class AmazonJobsClient {
    static final int MAX_RESULT_LIMIT = 100;
    static final int MAX_RESPONSE_CHARACTERS = 2_000_000;
    private static final URI SEARCH_ENDPOINT = URI.create("https://www.amazon.jobs/en/search.json");

    private final SourceHttpClient sourceHttpClient;

    public AmazonJobsClient(SourceHttpClient sourceHttpClient) {
        this.sourceHttpClient = sourceHttpClient;
    }

    public String fetchSearchPage(String query, int offset, int resultLimit) {
        String validatedQuery = JobQuery.requireValid(query);
        if (offset < 0 || resultLimit < 1 || resultLimit > MAX_RESULT_LIMIT) {
            throw new IllegalArgumentException("Amazon pagination parameters are out of range");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(SEARCH_ENDPOINT)
                .queryParam("base_query", validatedQuery)
                .queryParam("offset", offset)
                .queryParam("result_limit", resultLimit)
                .queryParam("sort", "recent");
        if (JobQuery.isIndiaSearch(validatedQuery)) {
            builder.queryParam("loc_query", "India");
        }

        String response = sourceHttpClient.get(builder.build().encode().toUri(), "Amazon Jobs");
        if (response.length() > MAX_RESPONSE_CHARACTERS) {
            throw new IllegalStateException("Amazon Jobs response exceeds the 2 MB safety limit");
        }
        return response;
    }
}
