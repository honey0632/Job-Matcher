package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.InfosysJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.InfosysJobsParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InfosysJobProvider implements JobProvider {
    private final InfosysJobsClient infosysJobsClient;
    private final InfosysJobsParser infosysJobsParser;

    public InfosysJobProvider(InfosysJobsClient infosysJobsClient, InfosysJobsParser infosysJobsParser) {
        this.infosysJobsClient = infosysJobsClient;
        this.infosysJobsParser = infosysJobsParser;
    }

    @Override
    public List<Jobs> fetch(String query) {
        String html = infosysJobsClient.fetchSearchPage(query);
        return infosysJobsParser.parse(html);
    }

    @Override
    public JobSource source() {
        return JobSource.INFOSYS;
    }
}