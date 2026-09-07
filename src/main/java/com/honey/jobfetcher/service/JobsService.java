// Coordinates job retrieval and search operations.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.client.GoogleCareersClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.GoogleCareersParser;
import com.honey.jobfetcher.repository.JobsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class JobsService {

    private final JobsRepository jobsRepository;
    private final GoogleCareersClient googleCareersClient;
    private final GoogleCareersParser googleCareersParser;

    public JobsService(
            JobsRepository jobsRepository,
            GoogleCareersClient googleCareersClient,
            GoogleCareersParser googleCareersParser
    ) {
        this.jobsRepository = jobsRepository;
        this.googleCareersClient = googleCareersClient;
        this.googleCareersParser = googleCareersParser;
    }

    public List<Jobs> fetchAndSaveGoogleJobs(String query) {
        String html = googleCareersClient.fetchSearchPage(query);
        List<Jobs> fetchedJobs = googleCareersParser.parse(html);
        LocalDateTime now = LocalDateTime.now();

        return fetchedJobs.stream()
                .map(job -> upsertJob(job, now))
                .toList();
    }

    public List<Jobs> getAllJobs(){
        return jobsRepository.findAll();
    }

    public Jobs getJobById(Long id) {
        return jobsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Job not found with id: " + id
                ));
    }

    public Jobs saveJob(Jobs job){
        return jobsRepository.save(job);
    }

    private Jobs upsertJob(Jobs fetchedJob, LocalDateTime now) {
        Jobs job = jobsRepository.findByExternalId(fetchedJob.getExternalId())
                .orElse(fetchedJob);

        job.setExternalId(fetchedJob.getExternalId());
        job.setTitle(fetchedJob.getTitle());
        job.setDescription(fetchedJob.getDescription());
        job.setCompany(fetchedJob.getCompany());
        job.setLocation(fetchedJob.getLocation());
        job.setJobUrl(fetchedJob.getJobUrl());
        job.setSource(fetchedJob.getSource());
        job.setUpdatedAt(now);

        if (job.getCreatedAt() == null) {
            job.setCreatedAt(now);
        }
        if (job.getStatus() == null) {
            job.setStatus(Jobs.STATUS_NO_ACTION);
        }

        return jobsRepository.save(job);
    }

}
