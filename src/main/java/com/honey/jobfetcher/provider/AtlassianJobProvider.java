package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.AtlassianJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.AtlassianJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtlassianJobProvider implements JobProvider {
    private final AtlassianJobsClient atlassianJobsClient;
    private final AtlassianJobsParser atlassianJobsParser;

    public AtlassianJobProvider(AtlassianJobsClient atlassianJobsClient, AtlassianJobsParser atlassianJobsParser) {
        this.atlassianJobsClient = atlassianJobsClient;
        this.atlassianJobsParser = atlassianJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = atlassianJobsClient.fetchSearchPage(query);
        return atlassianJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.ATLASSIAN;
    }
}