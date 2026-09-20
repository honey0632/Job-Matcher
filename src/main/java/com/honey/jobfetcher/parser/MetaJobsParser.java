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
public class MetaJobsParser {
    public List<Jobs> parse(String html) {
        List<Jobs> jobs = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements jobElements = doc.select("div[data-testid='job-card']");
        for (Element jobElement : jobElements) {
            Jobs job = new Jobs();

            String title = jobElement.select("h2").text();
            String location = jobElement.select("span[data-testid='location']").text();
            String id = jobElement.attr("data-jobid");
            String url = "https://www.metacareers.com/jobs/" + id;

            job.setTitle(title);
            job.setLocation(location);
            job.setJobUrl(url);
            job.setExternalId(JobSource.META.name() + ":" + id);
            job.setSource(JobSource.META.name());

            jobs.add(job);
        }

        return jobs;
    }
}