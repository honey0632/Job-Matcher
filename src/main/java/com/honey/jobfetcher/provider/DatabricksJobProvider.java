package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.DatabricksJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.DatabricksJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabricksJobProvider implements JobProvider {
    private final DatabricksJobsClient client;
    private final DatabricksJobsParser parser;

    public DatabricksJobProvider(DatabricksJobsClient client, DatabricksJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String json = client.fetchSearchPage(query);
        return parser.parse(json);
    }

    @Override
    public JobSource source() {
        return JobSource.DATABRICKS;
    }
}