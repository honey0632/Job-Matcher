package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CiscoJobsParser {
    public List<Jobs> parse(String html) {
        List<Jobs> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        for (Element jobElement : doc.select("div.job-card")) {
            String id = jobElement.attr("data-job-id");
            Jobs job = new Jobs();
            job.setTitle(jobElement.select("h3.job-title").text());
            job.setLocation(jobElement.select("span.job-location").text());
            job.setJobUrl("https://www.cisco.com" + jobElement.select("a.job-link").attr("href"));
            job.setExternalId(JobSource.CISCO.name() + ":" + id);
            job.setSource(JobSource.CISCO.name());
            jobs.add(job);
        }

        return jobs;
    }
}
