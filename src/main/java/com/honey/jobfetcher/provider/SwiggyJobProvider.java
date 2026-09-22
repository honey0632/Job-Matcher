package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.SwiggyJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.SwiggyJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SwiggyJobProvider implements JobProvider {
    private final SwiggyJobsClient client;
    private final SwiggyJobsParser parser;

    public SwiggyJobProvider(SwiggyJobsClient client, SwiggyJobsParser parser) {
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
        return JobSource.SWIGGY;
    }
}