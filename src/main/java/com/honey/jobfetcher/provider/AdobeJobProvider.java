package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.AdobeJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.AdobeJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdobeJobProvider implements JobProvider {
    private final AdobeJobsClient adobeJobsClient;
    private final AdobeJobsParser adobeJobsParser;

    public AdobeJobProvider(AdobeJobsClient adobeJobsClient, AdobeJobsParser adobeJobsParser) {
        this.adobeJobsClient = adobeJobsClient;
        this.adobeJobsParser = adobeJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = adobeJobsClient.fetchSearchPage(query);
        return adobeJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.ADOBE;
    }
}