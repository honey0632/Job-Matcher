package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.WiproJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.WiproJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WiproJobProvider implements JobProvider {
    private final WiproJobsClient wiproJobsClient;
    private final WiproJobsParser wiproJobsParser;

    public WiproJobProvider(WiproJobsClient wiproJobsClient, WiproJobsParser wiproJobsParser) {
        this.wiproJobsClient = wiproJobsClient;
        this.wiproJobsParser = wiproJobsParser;
    }

    @Override
    public List<Jobs> fetch(String query) {
        String html = wiproJobsClient.fetchSearchPage(query);
        return wiproJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.WIPRO;
    }
}