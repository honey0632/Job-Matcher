package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.AppleJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.AppleJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppleJobProvider implements JobProvider {
    private final AppleJobsClient appleJobsClient;
    private final AppleJobsParser appleJobsParser;

    public AppleJobProvider(AppleJobsClient appleJobsClient, AppleJobsParser appleJobsParser) {
        this.appleJobsClient = appleJobsClient;
        this.appleJobsParser = appleJobsParser;
    }

    @Override
    public List<Jobs> fetch(String query) {
        String html = appleJobsClient.fetchSearchPage(query);
        return appleJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.APPLE;
    }
}