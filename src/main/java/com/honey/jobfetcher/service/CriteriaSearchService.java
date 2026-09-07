// Searches external jobs and applies user criteria to the results.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.dto.JobSearchRequest;
import com.honey.jobfetcher.exception.InvalidResumeException;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CriteriaSearchService {

    private static final int MATCH_THRESHOLD = -1;
    private static final int MAX_RESULTS = 100;

    private final JobsService jobsService;
    private final JobMatchingService jobMatchingService;
    private final ResumeRepository resumeRepository;

    public CriteriaSearchService(
            JobsService jobsService,
            JobMatchingService jobMatchingService,
            ResumeRepository resumeRepository
    ) {
        this.jobsService = jobsService;
        this.jobMatchingService = jobMatchingService;
        this.resumeRepository = resumeRepository;
    }

    public List<JobMatchResponse> search(User user, JobSearchRequest request) {
        // Use the same criteria for the external search and the local match filter.
        String query = request.desiredRole().trim()
                + " " + request.country().trim()
                + " " + request.experienceYears() + " years experience";

        jobsService.fetchAndSaveGoogleJobs(query);

        Resume resume = resumeRepository
                .findTopByUserIdAndStatusOrderByUploadedAtDesc(user.getId(), "EXTRACTED")
                .orElseThrow(() -> new InvalidResumeException(
                        "Upload and extract a resume before searching for matches"
                ));

        return jobMatchingService.findMatches(resume.getId(), MAX_RESULTS, request.country())
                .stream()
                .filter(match -> match.score() > MATCH_THRESHOLD)
                .toList();
    }
}
