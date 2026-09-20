package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.IntuitJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.IntuitJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntuitJobProvider implements JobProvider {
    private final IntuitJobsClient intuitJobsClient;
    private final IntuitJobsParser intuitJobsParser;

    public IntuitJobProvider(IntuitJobsClient intuitJobsClient, IntuitJobsParser intuitJobsParser) {
        this.intuitJobsClient = intuitJobsClient;
        this.intuitJobsParser = intuitJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = intuitJobsClient.fetchSearchPage(query);
        return intuitJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.INTUIT;
    }
}