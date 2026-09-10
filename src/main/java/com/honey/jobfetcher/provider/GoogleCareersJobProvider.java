package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.GoogleCareersClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.GoogleCareersParser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapts the existing Google Careers client and parser to the provider contract.
 */
@Component
public class GoogleCareersJobProvider implements JobProvider {
    private final GoogleCareersClient client;
    private final GoogleCareersParser parser;

    public GoogleCareersJobProvider(GoogleCareersClient client, GoogleCareersParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public JobSource source() {
        return JobSource.GOOGLE_CAREERS;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        JobQuery.requireValid(query);
        return parser.parse(client.fetchSearchPage(query));
    }
}
