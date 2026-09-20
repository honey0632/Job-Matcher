package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.ServiceNowJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.ServiceNowJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceNowJobProvider implements JobProvider {
    private final ServiceNowJobsClient servicenowJobsClient;
    private final ServiceNowJobsParser servicenowJobsParser;

    public ServiceNowJobProvider(ServiceNowJobsClient servicenowJobsClient, ServiceNowJobsParser servicenowJobsParser) {
        this.servicenowJobsClient = servicenowJobsClient;
        this.servicenowJobsParser = servicenowJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = servicenowJobsClient.fetchSearchPage(query);
        return servicenowJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.SERVICENOW;
    }
}