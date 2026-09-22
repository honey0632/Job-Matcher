package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.PostmanJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.PostmanJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostmanJobProvider implements JobProvider {
    private final PostmanJobsClient client;
    private final PostmanJobsParser parser;

    public PostmanJobProvider(PostmanJobsClient client, PostmanJobsParser parser) {
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
        return JobSource.POSTMAN;
    }
}