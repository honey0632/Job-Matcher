// Exposes resume-to-job matching endpoints.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.ResumeRepository;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.JobMatchingService;
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

    private final JobMatchingService jobMatchingService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ResumeRepository resumeRepository;

    public JobMatchingController(
            JobMatchingService jobMatchingService,
            AuthenticatedUserService authenticatedUserService,
            ResumeRepository resumeRepository
    ) {
        this.jobMatchingService = jobMatchingService;
        this.authenticatedUserService = authenticatedUserService;
        this.resumeRepository = resumeRepository;
    }

    @GetMapping("/matches")
    public List<JobMatchResponse> findMatches(
            Authentication authentication,
            @RequestParam(defaultValue = "80") int threshold,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "") String location
    ) {
        // Validate the caller-provided threshold before querying the authenticated resume.
        if (threshold < -100 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be between -100 and 100");
        }

        User user = authenticatedUserService.requireUser(authentication);
        Resume resume = resumeRepository
                .findTopByUserIdAndStatusOrderByUploadedAtDesc(user.getId(), "EXTRACTED")
                .orElseThrow(() -> new InvalidResumeException(
                        "Upload and extract a resume before requesting matches"
                ));

        return jobMatchingService.findMatches(resume.getId(), limit, location)
                .stream()
                .filter(match -> match.score() > threshold)
                .toList();
    }
}
