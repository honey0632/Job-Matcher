package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.OracleJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.OracleJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OracleJobProvider implements JobProvider {
    private final OracleJobsClient oracleJobsClient;
    private final OracleJobsParser oracleJobsParser;

    public OracleJobProvider(OracleJobsClient oracleJobsClient, OracleJobsParser oracleJobsParser) {
        this.oracleJobsClient = oracleJobsClient;
        this.oracleJobsParser = oracleJobsParser;
    }

    @Override
    public List<Jobs> fetchJobs(String query) {
        String html = oracleJobsClient.fetchSearchPage(query);
        return oracleJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.ORACLE;
    }
}