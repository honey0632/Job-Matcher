package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.model.SavedJob;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.JobsRepository;
import com.honey.jobfetcher.repository.SavedJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavedJobService {

    private static final Logger logger = LoggerFactory.getLogger(SavedJobService.class);

    private final SavedJobRepository savedJobRepository;
    private final JobsRepository jobsRepository;

    public SavedJobService(SavedJobRepository savedJobRepository, JobsRepository jobsRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobsRepository = jobsRepository;
    }

    public List<JobMatchResponse> getSavedJobs(User user) {
        return savedJobRepository.findByUserIdOrderBySavedAtDesc(user.getId())
                .stream()
                .map(savedJob -> JobMatchResponse.from(savedJob.getJob(), savedJob.getScore() != null ? savedJob.getScore() : 85))
                .toList();
    }

    public List<Long> getSavedJobIds(User user) {
        return savedJobRepository.findByUserIdOrderBySavedAtDesc(user.getId())
                .stream()
                .map(savedJob -> savedJob.getJob().getId())
                .toList();
    }

    @Transactional
    public JobMatchResponse saveJob(User user, Long jobId, Integer score) {
        Jobs job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));

        SavedJob savedJob = savedJobRepository.findByUserIdAndJobId(user.getId(), jobId)
                .orElseGet(() -> {
                    SavedJob sj = new SavedJob();
                    sj.setUser(user);
                    sj.setJob(job);
                    sj.setSavedAt(LocalDateTime.now());
                    return sj;
                });

        if (score != null) {
            savedJob.setScore(score);
        } else if (savedJob.getScore() == null) {
            savedJob.setScore(85);
        }

        savedJob = savedJobRepository.save(savedJob);
        return JobMatchResponse.from(savedJob.getJob(), savedJob.getScore());
    }

    @Transactional
    public void unsaveJob(User user, Long jobId) {
        savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
    }

    @Transactional
    public void autoSaveHighMatches(User user, List<JobMatchResponse> matches) {
        if (user == null || matches == null || matches.isEmpty()) {
            return;
        }
        for (JobMatchResponse match : matches) {
            if (match.jobId() != null && match.score() > 80) {
                try {
                    saveJob(user, match.jobId(), match.score());
                } catch (Exception exception) {
                    logger.warn("Failed to auto-save match jobId={}: {}", match.jobId(), exception.getMessage());
                }
            }
        }
    }
}
