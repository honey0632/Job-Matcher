package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.PayPalJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.PayPalJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayPalJobProvider implements JobProvider {
    private final PayPalJobsClient client;
    private final PayPalJobsParser parser;

    public PayPalJobProvider(PayPalJobsClient client, PayPalJobsParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String json = client.fetchSearchPage(query);
        return parser.parse(json);
    }

    @Override
    public JobSource source() {
        return JobSource.PAYPAL;
    }
}