package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.TcsJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.TcsJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TcsJobProvider implements JobProvider {
    private final TcsJobsClient tcsJobsClient;
    private final TcsJobsParser tcsJobsParser;

    public TcsJobProvider(TcsJobsClient tcsJobsClient, TcsJobsParser tcsJobsParser) {
        this.tcsJobsClient = tcsJobsClient;
        this.tcsJobsParser = tcsJobsParser;
    }

    @Override
    public List<Jobs> fetch(String query) {
        String html = tcsJobsClient.fetchSearchPage(query);
        return tcsJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.TCS;
    }
}