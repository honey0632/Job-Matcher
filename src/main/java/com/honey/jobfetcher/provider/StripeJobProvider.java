package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.StripeJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.StripeJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StripeJobProvider implements JobProvider {
    private final StripeJobsClient client;
    private final StripeJobsParser parser;

    public StripeJobProvider(StripeJobsClient client, StripeJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String json = client.fetchSearchPage(query);
        return parser.parse(json);
    }

    @Override
    public JobSource source() {
        return JobSource.STRIPE;
    }
}