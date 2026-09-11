// Exposes resume-to-job matching endpoints.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.ResumeRepository;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.JobMatchingService;
import com.honey.jobfetcher.service.SavedJobService;
import com.honey.jobfetcher.exception.InvalidResumeException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobMatchingController {

        // A 0% threshold exposes every result returned by the matching provider.
    private static final int MATCH_THRESHOLD = 0;

    private final JobMatchingService jobMatchingService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ResumeRepository resumeRepository;
    private final SavedJobService savedJobService;

    public JobMatchingController(
            JobMatchingService jobMatchingService,
            AuthenticatedUserService authenticatedUserService,
            ResumeRepository resumeRepository,
            SavedJobService savedJobService
    ) {
        this.jobMatchingService = jobMatchingService;
        this.authenticatedUserService = authenticatedUserService;
        this.resumeRepository = resumeRepository;
        this.savedJobService = savedJobService;
    }

    @GetMapping("/matches")
    public List<JobMatchResponse> findMatches(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "") String location
    ) {
        User user = authenticatedUserService.requireUser(authentication);
        Resume resume = resumeRepository
                .findTopByUserIdAndStatusOrderByUploadedAtDesc(user.getId(), "EXTRACTED")
                .orElseThrow(() -> new InvalidResumeException(
                        "Upload and extract a resume before requesting matches"
                ));

        List<JobMatchResponse> matches = jobMatchingService.findMatches(resume.getId(), limit, location)
                .stream()
                .filter(match -> match.score() >= MATCH_THRESHOLD)
                .toList();

        savedJobService.autoSaveHighMatches(user, matches);

        return matches;
    }
}
