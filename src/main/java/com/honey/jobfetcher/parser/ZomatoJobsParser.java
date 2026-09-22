package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.provider.JobSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ZomatoJobsParser {
    public List<Jobs> parse(String html) {
        List<Jobs> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements jobElements = doc.select("div.job-card");
        for (Element jobElement : jobElements) {
            Jobs job = new Jobs();
            String title = jobElement.select("h3.job-title").text();
            String location = jobElement.select("span.job-location").text();
            String id = jobElement.attr("data-job-id");
            String url = "https://zomato.com" + jobElement.select("a.job-link").attr("href");

            job.setTitle(title);
            job.setCompany("Zomato");
            job.setLocation(location);
            job.setJobUrl(url);
            job.setExternalId(JobSource.ZOMATO.name() + ":" + id);
            job.setSource(JobSource.ZOMATO.name());
            job.setStatus(Jobs.STATUS_NO_ACTION);
            jobs.add(job);
        }
        return jobs;
    }
}