package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.NetflixJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.NetflixJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NetflixJobProvider implements JobProvider {
    private final NetflixJobsClient netflixJobsClient;
    private final NetflixJobsParser netflixJobsParser;

    public NetflixJobProvider(NetflixJobsClient netflixJobsClient, NetflixJobsParser netflixJobsParser) {
        this.netflixJobsClient = netflixJobsClient;
        this.netflixJobsParser = netflixJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = netflixJobsClient.fetchSearchPage(query);
        return netflixJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.NETFLIX;
    }
}