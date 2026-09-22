package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.RazorpayJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.RazorpayJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RazorpayJobProvider implements JobProvider {
    private final RazorpayJobsClient client;
    private final RazorpayJobsParser parser;

    public RazorpayJobProvider(RazorpayJobsClient client, RazorpayJobsParser parser) {
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
        return JobSource.RAZORPAY;
    }
}