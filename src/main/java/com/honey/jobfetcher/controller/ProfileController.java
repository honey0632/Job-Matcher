// Exposes profile and search-preference endpoints.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.dto.SearchPreferenceRequest;
import com.honey.jobfetcher.dto.SearchPreferenceResponse;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.SearchPreferenceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/preferences")
public class ProfileController {

    private final AuthenticatedUserService authenticatedUserService;
    private final SearchPreferenceService preferenceService;

    public ProfileController(
            AuthenticatedUserService authenticatedUserService,
            SearchPreferenceService preferenceService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public SearchPreferenceResponse get(Authentication authentication) {
        User user = authenticatedUserService.requireUser(authentication);
        return preferenceService.get(user);
    }

    @PutMapping
    public SearchPreferenceResponse save(
            Authentication authentication,
            @Valid @RequestBody SearchPreferenceRequest request
    ) {
        User user = authenticatedUserService.requireUser(authentication);
        return preferenceService.save(user, request);
    }
}
