// Verifies criteria-based job search and filtering behavior.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.dto.JobSearchRequest;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriteriaSearchServiceTest {

    @Mock
    private JobsService jobsService;

    @Mock
    private JobMatchingService jobMatchingService;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private SavedJobService savedJobService;

    @Test
    void returnsMatchesAtOrAboveZeroPercentDuringProviderVerification() {
        // The 0% threshold keeps source-specific provider results visible.
        User user = new User();
        user.setId(7L);

        Resume resume = new Resume();
        resume.setId(11L);

        when(resumeRepository.findTopByUserIdAndStatusOrderByUploadedAtDesc(7L, "EXTRACTED"))
                .thenReturn(Optional.of(resume));
        when(jobMatchingService.findMatches(11L, 100, "India", null)).thenReturn(List.of(
                new JobMatchResponse(1L, "high", "High", "Google", "India", "Description", "url", 1),
                new JobMatchResponse(2L, "low", "Low", "Google", "India", "Description", "url", 0)
        ));

        CriteriaSearchService service = new CriteriaSearchService(
                jobsService,
                jobMatchingService,
                resumeRepository,
                savedJobService
        );

        List<JobMatchResponse> results = service.search(
                user,
                new JobSearchRequest("India", 3, "Backend Engineer")
        );

        assertEquals(2, results.size());
        assertEquals("high", results.get(0).externalId());
        verify(jobsService).fetchAndSaveApprovedJobs(anyString());
        verify(savedJobService).autoSaveHighMatches(user, results);
    }

    @Test
    void forwardsSourceToMatchingServiceAndFiltersTargetSource() {
        User user = new User();
        user.setId(7L);

        Resume resume = new Resume();
        resume.setId(11L);

        when(resumeRepository.findTopByUserIdAndStatusOrderByUploadedAtDesc(7L, "EXTRACTED"))
                .thenReturn(Optional.of(resume));
        when(jobMatchingService.findMatches(11L, 100, "India", "AMAZON")).thenReturn(List.of(
                new JobMatchResponse(1L, "AMAZON:1", "Amazon Role", "Amazon", "India", "Description", "url", 90),
                new JobMatchResponse(2L, "GOOGLE:1", "Google Role", "Google", "India", "Description", "url", 95)
        ));

        CriteriaSearchService service = new CriteriaSearchService(
                jobsService,
                jobMatchingService,
                resumeRepository,
                savedJobService
        );

        List<JobMatchResponse> results = service.search(
                user,
                new JobSearchRequest("India", 3, "Backend Engineer", "AMAZON")
        );

        assertEquals(1, results.size());
        assertEquals("AMAZON:1", results.get(0).externalId());
        verify(savedJobService).autoSaveHighMatches(user, results);
    }
}
