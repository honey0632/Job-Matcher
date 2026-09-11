// Exposes job search and listing endpoints.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.dto.JobSearchRequest;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.CriteriaSearchService;
import com.honey.jobfetcher.service.JobsService;
import com.honey.jobfetcher.service.SavedJobService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobsController {

    private final JobsService jobsService;
    private final AuthenticatedUserService authenticatedUserService;
    private final CriteriaSearchService criteriaSearchService;
    private final SavedJobService savedJobService;

    public JobsController(
            JobsService jobsService,
            AuthenticatedUserService authenticatedUserService,
            CriteriaSearchService criteriaSearchService,
            SavedJobService savedJobService
    ) {
        this.jobsService = jobsService;
        this.authenticatedUserService = authenticatedUserService;
        this.criteriaSearchService = criteriaSearchService;
        this.savedJobService = savedJobService;
    }

    @GetMapping
    public List<Jobs> getAlljobs(){
        return jobsService.getAllJobs();
    }

    @GetMapping("/saved")
    public List<JobMatchResponse> getSavedJobs(Authentication authentication) {
        User user = authenticatedUserService.requireUser(authentication);
        return savedJobService.getSavedJobs(user);
    }

    @GetMapping("/saved/ids")
    public List<Long> getSavedJobIds(Authentication authentication) {
        User user = authenticatedUserService.requireUser(authentication);
        return savedJobService.getSavedJobIds(user);
    }

    @PostMapping("/saved/{jobId}")
    public JobMatchResponse saveJob(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestParam(required = false) Integer score
    ) {
        User user = authenticatedUserService.requireUser(authentication);
        return savedJobService.saveJob(user, jobId, score);
    }

    @DeleteMapping("/saved/{jobId}")
    public void unsaveJob(
            Authentication authentication,
            @PathVariable Long jobId
    ) {
        User user = authenticatedUserService.requireUser(authentication);
        savedJobService.unsaveJob(user, jobId);
    }

    @GetMapping("/{id}")
    public Jobs getJobById(@PathVariable Long id){
        return jobsService.getJobById(id);
    }

    @GetMapping("/search")
    public List<Jobs> searchGoogleJobs(@RequestParam String query) {
        return jobsService.fetchAndSaveGoogleJobs(query);
    }

    @PostMapping("/search")
    public List<JobMatchResponse> searchByCriteria(
            Authentication authentication,
            @Valid @RequestBody JobSearchRequest request
    ) {
        User user = authenticatedUserService.requireUser(authentication);
        return criteriaSearchService.search(user, request);
    }
}
