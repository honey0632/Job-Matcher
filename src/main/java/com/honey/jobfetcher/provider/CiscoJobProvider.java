package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.CiscoJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.CiscoJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CiscoJobProvider implements JobProvider {
    private final CiscoJobsClient ciscoJobsClient;
    private final CiscoJobsParser ciscoJobsParser;

    public CiscoJobProvider(CiscoJobsClient ciscoJobsClient, CiscoJobsParser ciscoJobsParser) {
        this.ciscoJobsClient = ciscoJobsClient;
        this.ciscoJobsParser = ciscoJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = ciscoJobsClient.fetchSearchPage(query);
        return ciscoJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.CISCO;
    }
}