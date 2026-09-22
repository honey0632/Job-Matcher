package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.SnowflakeJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.SnowflakeJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SnowflakeJobProvider implements JobProvider {
    private final SnowflakeJobsClient client;
    private final SnowflakeJobsParser parser;

    public SnowflakeJobProvider(SnowflakeJobsClient client, SnowflakeJobsParser parser) {
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
        return JobSource.SNOWFLAKE;
    }
}