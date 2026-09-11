// Searches external jobs and applies user criteria to the results.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.dto.JobSearchRequest;
import com.honey.jobfetcher.exception.InvalidResumeException;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.provider.JobSource;
import com.honey.jobfetcher.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CriteriaSearchService {

    private static final int MATCH_THRESHOLD = 80;
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
        String query = request.desiredRole().trim();
        String sourceFilter = request.source();

        if (sourceFilter != null && !sourceFilter.isBlank() && !"ALL".equalsIgnoreCase(sourceFilter)) {
            try {
                JobSource jobSource = JobSource.valueOf(sourceFilter.trim().toUpperCase(Locale.ROOT));
                jobsService.fetchAndSaveForSource(jobSource, query);
            } catch (IllegalArgumentException e) {
                jobsService.fetchAndSaveApprovedJobs(query);
            }
        } else {
            jobsService.fetchAndSaveApprovedJobs(query);
        }

        Resume resume = resumeRepository
                .findTopByUserIdAndStatusOrderByUploadedAtDesc(user.getId(), "EXTRACTED")
                .orElseThrow(() -> new InvalidResumeException(
                        "Upload and extract a resume before searching for matches"
                ));

        List<JobMatchResponse> matches = jobMatchingService.findMatches(resume.getId(), MAX_RESULTS, request.country(), sourceFilter)
                .stream()
                .filter(match -> match.score() > MATCH_THRESHOLD)
                .toList();

        if (sourceFilter != null && !sourceFilter.isBlank() && !"ALL".equalsIgnoreCase(sourceFilter)) {
            String filter = sourceFilter.trim().toLowerCase(Locale.ROOT);
            matches = matches.stream()
                    .filter(m -> matchesSource(m, filter))
                    .toList();
        }

        return matches;
    }

    private boolean matchesSource(JobMatchResponse match, String filter) {
        if (match.source() != null && match.source().toLowerCase(Locale.ROOT).contains(filter)) {
            return true;
        }
        if (match.company() != null && match.company().toLowerCase(Locale.ROOT).contains(filter)) {
            return true;
        }
        if ("google_careers".equals(filter) || "google".equals(filter)) {
            return match.company() != null && match.company().toLowerCase(Locale.ROOT).contains("google");
        }
        if ("wells_fargo".equals(filter) || "wells".equals(filter)) {
            return match.company() != null && match.company().toLowerCase(Locale.ROOT).contains("wells");
        }
        return false;
    }
}
