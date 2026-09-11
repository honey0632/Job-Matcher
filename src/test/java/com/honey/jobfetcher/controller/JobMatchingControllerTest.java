package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.ResumeRepository;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.JobMatchingService;
import com.honey.jobfetcher.service.SavedJobService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobMatchingControllerTest {

    @Test
        void returnsMatchesAtOrAboveZeroPercent() {
        AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);
        JobMatchingService jobMatchingService = mock(JobMatchingService.class);
        ResumeRepository resumeRepository = mock(ResumeRepository.class);
        SavedJobService savedJobService = mock(SavedJobService.class);
        Authentication authentication = mock(Authentication.class);

        User user = new User();
        user.setId(7L);
        Resume resume = new Resume();
        resume.setId(11L);

        when(authenticatedUserService.requireUser(authentication)).thenReturn(user);
        when(resumeRepository.findTopByUserIdAndStatusOrderByUploadedAtDesc(7L, "EXTRACTED"))
                .thenReturn(Optional.of(resume));
        when(jobMatchingService.findMatches(11L, 20, "")).thenReturn(List.of(
                new JobMatchResponse(1L, "high", "High", "Google", "India", "Description", "url", 1),
                new JobMatchResponse(2L, "boundary", "Boundary", "Google", "India", "Description", "url", 0)
        ));

        JobMatchingController controller = new JobMatchingController(
                jobMatchingService,
                authenticatedUserService,
                resumeRepository,
                savedJobService
        );

        List<JobMatchResponse> results = controller.findMatches(authentication, 20, "");

        assertEquals(2, results.size());
        assertEquals("high", results.getFirst().externalId());
    }
}
