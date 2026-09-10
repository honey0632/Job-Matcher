package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.NvidiaJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.NvidiaJobsParser;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads at most 25 title-like matches from NVIDIA's public sitemap.
 */
@Component
public class NvidiaJobProvider implements JobProvider {
    static final int MAX_CANDIDATE_PAGES = 25;

    private final NvidiaJobsClient client;
    private final NvidiaJobsParser parser;

    public NvidiaJobProvider(NvidiaJobsClient client, NvidiaJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public JobSource source() {
        return JobSource.NVIDIA;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        JobQuery.requireValid(query);
        List<URI> candidates = parser.parseSitemap(client.fetchSitemap()).stream()
                .filter(page -> JobQuery.matchesText(query, page.getPath()))
                .limit(MAX_CANDIDATE_PAGES)
                .toList();

        List<Jobs> jobs = new ArrayList<>();
        Set<String> seenExternalIds = new HashSet<>();
        for (URI candidate : candidates) {
            Jobs job = parser.parseJobPage(client.fetchPublicJobPage(candidate), candidate);
            if (seenExternalIds.add(job.getExternalId())) {
                jobs.add(job);
            }
        }
        return jobs;
    }
}
