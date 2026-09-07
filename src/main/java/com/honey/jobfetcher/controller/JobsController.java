// Exposes job search and listing endpoints.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.dto.JobSearchRequest;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.CriteriaSearchService;
import com.honey.jobfetcher.service.JobsService;
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

    public JobsController(
            JobsService jobsService,
            AuthenticatedUserService authenticatedUserService,
            CriteriaSearchService criteriaSearchService
    ) {
        this.jobsService = jobsService;
        this.authenticatedUserService = authenticatedUserService;
        this.criteriaSearchService = criteriaSearchService;
    }

    @GetMapping
    public List<Jobs> getAlljobs(){
        return jobsService.getAllJobs();
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
