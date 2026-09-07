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

    @Test
    void returnsMatchesAboveEightyPercent() {
        // The test confirms that only strong matches are included while the
        // requested search location is preserved.
        User user = new User();
        user.setId(7L);

        Resume resume = new Resume();
        resume.setId(11L);

        when(resumeRepository.findTopByUserIdAndStatusOrderByUploadedAtDesc(7L, "EXTRACTED"))
                .thenReturn(Optional.of(resume));
        when(jobMatchingService.findMatches(11L, 100, "India")).thenReturn(List.of(
                new JobMatchResponse(1L, "high", "High", "Google", "India", "Description", "url", 81),
                new JobMatchResponse(2L, "low", "Low", "Google", "India", "Description", "url", 80)
        ));

        CriteriaSearchService service = new CriteriaSearchService(
                jobsService,
                jobMatchingService,
                resumeRepository
        );

        List<JobMatchResponse> results = service.search(
                user,
                new JobSearchRequest("India", 3, "Backend Engineer")
        );

        assertEquals(1, results.size());
        assertEquals("high", results.get(0).externalId());
        verify(jobsService).fetchAndSaveGoogleJobs(anyString());
    }
}
