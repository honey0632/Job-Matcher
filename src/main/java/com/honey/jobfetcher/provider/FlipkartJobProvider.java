package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.FlipkartJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.FlipkartJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlipkartJobProvider implements JobProvider {
    private final FlipkartJobsClient client;
    private final FlipkartJobsParser parser;

    public FlipkartJobProvider(FlipkartJobsClient client, FlipkartJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = client.fetchSearchPage(query);
        return parser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.FLIPKART;
    }
}