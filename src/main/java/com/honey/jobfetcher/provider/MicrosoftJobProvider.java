package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.MicrosoftCareersClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.MicrosoftJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MicrosoftJobProvider implements JobProvider {
    private final MicrosoftCareersClient microsoftCareersClient;
    private final MicrosoftJobsParser microsoftJobsParser;

    public MicrosoftJobProvider(MicrosoftCareersClient microsoftCareersClient, MicrosoftJobsParser microsoftJobsParser) {
        this.microsoftCareersClient = microsoftCareersClient;
        this.microsoftJobsParser = microsoftJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = microsoftCareersClient.fetchSearchPage(query);
        return microsoftJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.MICROSOFT;
    }
}