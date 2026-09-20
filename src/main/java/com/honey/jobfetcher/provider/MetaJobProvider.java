package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.MetaCareersClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.MetaJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetaJobProvider implements JobProvider {
    private final MetaCareersClient metaCareersClient;
    private final MetaJobsParser metaJobsParser;

    public MetaJobProvider(MetaCareersClient metaCareersClient, MetaJobsParser metaJobsParser) {
        this.metaCareersClient = metaCareersClient;
        this.metaJobsParser = metaJobsParser;
    }

    @Override
    public List<Jobs> fetch(String query) {
        String html = metaCareersClient.fetchSearchPage(query);
        return metaJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.META;
    }
}