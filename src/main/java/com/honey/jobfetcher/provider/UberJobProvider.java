package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.UberJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.UberJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UberJobProvider implements JobProvider {
    private final UberJobsClient uberJobsClient;
    private final UberJobsParser uberJobsParser;

    public UberJobProvider(UberJobsClient uberJobsClient, UberJobsParser uberJobsParser) {
        this.uberJobsClient = uberJobsClient;
        this.uberJobsParser = uberJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = uberJobsClient.fetchSearchPage(query);
        return uberJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.UBER;
    }
}