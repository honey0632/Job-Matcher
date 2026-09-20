package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AppleJobsParser {
    public List<Jobs> parse(String html) {
        List<Jobs> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements jobElements = doc.select("div.job-listing");
        for (Element jobElement : jobElements) {
            Jobs job = new Jobs();

            String title = jobElement.select("h2.job-title").text();
            String location = jobElement.select("span.job-location").text();
            String id = jobElement.attr("data-job-id");
            String url = "https://jobs.apple.com" + jobElement.select("a.job-link").attr("href");

            job.setTitle(title);
            job.setLocation(location);
            job.setJobUrl(url);
            job.setExternalId(JobSource.APPLE.name() + ":" + id);
            job.setSource(JobSource.APPLE.name());

            jobs.add(job);
        }

        return jobs;
    }
}