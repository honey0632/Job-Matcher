// Coordinates job retrieval and search operations.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.client.GoogleCareersClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.GoogleCareersParser;
import com.honey.jobfetcher.provider.ApprovedJobSourcesProperties;
import com.honey.jobfetcher.provider.GoogleCareersJobProvider;
import com.honey.jobfetcher.provider.JobProvider;
import com.honey.jobfetcher.provider.JobSource;
import com.honey.jobfetcher.repository.JobsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class JobsService {

    private static final Logger logger = LoggerFactory.getLogger(JobsService.class);

    private final JobsRepository jobsRepository;
    private final Map<JobSource, JobProvider> providers;
    private final ApprovedJobSourcesProperties approvedJobSources;

    @Autowired
    public JobsService(
            JobsRepository jobsRepository,
            List<JobProvider> providers,
            ApprovedJobSourcesProperties approvedJobSources
    ) {
        this.jobsRepository = jobsRepository;
        this.providers = indexProviders(providers);
        this.approvedJobSources = approvedJobSources;
    }

    /**
     * Retains the original unit-test construction path while Google is adapted
     * to the provider abstraction used by the application.
     */
    @Deprecated(forRemoval = false)
    public JobsService(
            JobsRepository jobsRepository,
            GoogleCareersClient googleCareersClient,
            GoogleCareersParser googleCareersParser
    ) {
        this(
                jobsRepository,
                List.of(new GoogleCareersJobProvider(googleCareersClient, googleCareersParser)),
                new ApprovedJobSourcesProperties()
        );
    }

    public List<Jobs> fetchAndSaveGoogleJobs(String query) {
        return fetchAndSave(providerFor(JobSource.GOOGLE_CAREERS), query);
    }

    /**
     * Fetches every configured approved provider sequentially and persists
     * results. A failing provider logs a warning and continues with remaining sources.
     */
    public List<Jobs> fetchAndSaveApprovedJobs(String query) {
        List<Jobs> savedJobs = new ArrayList<>();
        for (JobSource source : approvedJobSources.enabledSources()) {
            try {
                savedJobs.addAll(fetchAndSave(providerFor(source), query));
            } catch (RuntimeException exception) {
                logger.warn("Failed to fetch {} jobs: {}", source, exception.getMessage());
            }
        }
        return List.copyOf(savedJobs);
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

    private List<Jobs> fetchAndSave(JobProvider provider, String query) {
        LocalDateTime now = LocalDateTime.now();
        return provider.fetchJobs(query).stream()
                .map(job -> upsertJob(job, now))
                .toList();
    }

    private JobProvider providerFor(JobSource source) {
        JobProvider provider = providers.get(source);
        if (provider == null) {
            throw new IllegalStateException("No job provider is configured for " + source);
        }
        return provider;
    }

    private Map<JobSource, JobProvider> indexProviders(List<JobProvider> providers) {
        Map<JobSource, JobProvider> indexed = new EnumMap<>(JobSource.class);
        for (JobProvider provider : providers) {
            if (indexed.putIfAbsent(provider.source(), provider) != null) {
                throw new IllegalStateException("Multiple job providers are configured for " + provider.source());
            }
        }
        return Map.copyOf(indexed);
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
