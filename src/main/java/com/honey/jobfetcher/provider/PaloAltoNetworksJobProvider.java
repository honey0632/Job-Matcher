package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.PaloAltoNetworksJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.PaloAltoNetworksJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaloAltoNetworksJobProvider implements JobProvider {
    private final PaloAltoNetworksJobsClient client;
    private final PaloAltoNetworksJobsParser parser;

    public PaloAltoNetworksJobProvider(PaloAltoNetworksJobsClient client, PaloAltoNetworksJobsParser parser) {
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
        return JobSource.PALO_ALTO_NETWORKS;
    }
}