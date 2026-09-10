package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.AmazonJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.AmazonJobsParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fetches a small, sequentially paginated set of Amazon Jobs results.
 */
@Component
public class AmazonJobProvider implements JobProvider {
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 3;
    private static final int MAX_JOBS = 200;

    private final AmazonJobsClient client;
    private final AmazonJobsParser parser;

    public AmazonJobProvider(AmazonJobsClient client, AmazonJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public JobSource source() {
        return JobSource.AMAZON;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        JobQuery.requireValid(query);
        List<Jobs> results = new ArrayList<>();
        Set<String> seenExternalIds = new HashSet<>();

        for (int page = 0; page < MAX_PAGES && results.size() < MAX_JOBS; page++) {
            List<Jobs> pageJobs = parser.parse(client.fetchSearchPage(query, page * PAGE_SIZE, PAGE_SIZE));
            if (pageJobs.isEmpty()) {
                break;
            }

            int before = seenExternalIds.size();
            for (Jobs job : pageJobs) {
                if (seenExternalIds.add(job.getExternalId()) && results.size() < MAX_JOBS) {
                    results.add(job);
                }
            }
            if (seenExternalIds.size() == before || pageJobs.size() < PAGE_SIZE) {
                break;
            }
        }
        return results;
    }
}
