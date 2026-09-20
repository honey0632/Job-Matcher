package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.MuseJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.MuseJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UberJobProvider implements JobProvider {
    private final MuseJobsClient jobsClient;
    private final MuseJobsParser jobsParser;

    public UberJobProvider(MuseJobsClient jobsClient, MuseJobsParser jobsParser) {
        this.jobsClient = jobsClient;
        this.jobsParser = jobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        return jobsParser.parse(jobsClient.fetchJobs("Uber", query), source(), query);
    }

    @Override
    public JobSource source() {
        return JobSource.UBER;
    }
}