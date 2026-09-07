// Handles resume upload, download, and deletion endpoints.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.dto.ResumeResponse;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final AuthenticatedUserService authenticatedUserService;

    public ResumeController(
            ResumeService resumeService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.resumeService = resumeService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse upload(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        User user = authenticatedUserService.requireUser(authentication);
        return resumeService.upload(user, file);
    }

    @GetMapping
    public List<ResumeResponse> findByUser(Authentication authentication) {
        User user = authenticatedUserService.requireUser(authentication);
        return resumeService.findByUser(user);
    }
}
