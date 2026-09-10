package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.WellsFargoJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.WellsFargoJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetches and locally filters Wells Fargo's non-paginated public XML feed.
 */
@Component
public class WellsFargoJobProvider implements JobProvider {
    private final WellsFargoJobsClient client;
    private final WellsFargoJobsParser parser;

    public WellsFargoJobProvider(WellsFargoJobsClient client, WellsFargoJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public JobSource source() {
        return JobSource.WELLS_FARGO;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        JobQuery.requireValid(query);
        return parser.parse(client.fetchAllJobs(), query);
    }
}
