package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.ZomatoJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.ZomatoJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZomatoJobProvider implements JobProvider {
    private final ZomatoJobsClient client;
    private final ZomatoJobsParser parser;

    public ZomatoJobProvider(ZomatoJobsClient client, ZomatoJobsParser parser) {
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
        return JobSource.ZOMATO;
    }
}