package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.LinkedInJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.LinkedInJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkedInJobProvider implements JobProvider {
    private final LinkedInJobsClient client;
    private final LinkedInJobsParser parser;

    public LinkedInJobProvider(LinkedInJobsClient client, LinkedInJobsParser parser) {
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
        return JobSource.LINKEDIN;
    }
}