package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.SalesforceJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.SalesforceJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalesforceJobProvider implements JobProvider {
    private final SalesforceJobsClient salesforceJobsClient;
    private final SalesforceJobsParser salesforceJobsParser;

    public SalesforceJobProvider(SalesforceJobsClient salesforceJobsClient, SalesforceJobsParser salesforceJobsParser) {
        this.salesforceJobsClient = salesforceJobsClient;
        this.salesforceJobsParser = salesforceJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = salesforceJobsClient.fetchSearchPage(query);
        return salesforceJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.SALESFORCE;
    }
}