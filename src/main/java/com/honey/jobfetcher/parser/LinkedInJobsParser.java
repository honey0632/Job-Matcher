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
public class LinkedInJobsParser {
    public List<Jobs> parse(String html) {
        List<Jobs> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements jobElements = doc.select("li[data-occludable-job-id]");
        for (Element jobElement : jobElements) {
            Jobs job = new Jobs();
            String id = jobElement.attr("data-occludable-job-id");
            String title = jobElement.select("h3.base-search-card__title").text();
            String company = jobElement.select("h4.base-search-card__subtitle").text();
            String location = jobElement.select("span.job-search-card__location").text();
            String url = jobElement.select("a.base-card__full-link").attr("href");

            job.setExternalId(JobSource.LINKEDIN.name() + ":" + id);
            job.setTitle(title);
            job.setCompany(company);
            job.setLocation(location);
            job.setJobUrl(url);
            job.setSource(JobSource.LINKEDIN.name());
            job.setStatus(Jobs.STATUS_NO_ACTION);
            jobs.add(job);
        }
        return jobs;
    }
}