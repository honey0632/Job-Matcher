// Verifies local keyword-based resume matching behavior.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.repository.JobsRepository;
import com.honey.jobfetcher.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordJobMatchingServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobsRepository jobsRepository;

    @Test
    void ranksJobsByKeywordOverlap() {
        // Both jobs are in the requested country; only the relevant job should score.
        Resume resume = new Resume();
        resume.setExtractedText("Java Spring PostgreSQL backend engineer");

        Jobs matchingJob = new Jobs();
        matchingJob.setId(1L);
        matchingJob.setExternalId("job-1");
        matchingJob.setTitle("Java Spring Backend Engineer");
        matchingJob.setDescription("Build PostgreSQL services");
        matchingJob.setCompany("Google");
        matchingJob.setLocation("India");

        Jobs unrelatedJob = new Jobs();
        unrelatedJob.setId(2L);
        unrelatedJob.setExternalId("job-2");
        unrelatedJob.setTitle("UX Designer");
        unrelatedJob.setDescription("Design user experiences");
        unrelatedJob.setCompany("Google");
        unrelatedJob.setLocation("India");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(jobsRepository.findAll()).thenReturn(List.of(unrelatedJob, matchingJob));

        KeywordJobMatchingService service = new KeywordJobMatchingService(
                resumeRepository,
                jobsRepository
        );

        var matches = service.findMatches(1L, 10, "India");

        assertEquals(1, matches.size());
        assertEquals("job-1", matches.get(0).externalId());
        assertTrue(matches.get(0).score() > 0);
    }

    @Test
    void filtersBySourceAndMatchesCountryCodes() {
        Resume resume = new Resume();
        resume.setExtractedText("Java backend software engineer");

        Jobs amazonJob = new Jobs();
        amazonJob.setId(1L);
        amazonJob.setExternalId("AMAZON:1");
        amazonJob.setTitle("Java Software Engineer");
        amazonJob.setDescription("Java backend development");
        amazonJob.setCompany("Amazon");
        amazonJob.setSource("AMAZON");
        amazonJob.setLocation("Bengaluru, KA, IND");

        Jobs googleJob = new Jobs();
        googleJob.setId(2L);
        googleJob.setExternalId("GOOGLE:1");
        googleJob.setTitle("Java Software Engineer");
        googleJob.setDescription("Java backend development");
        googleJob.setCompany("Google");
        googleJob.setSource("GOOGLE_CAREERS");
        googleJob.setLocation("Bengaluru, India");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(jobsRepository.findAll()).thenReturn(List.of(googleJob, amazonJob));

        KeywordJobMatchingService service = new KeywordJobMatchingService(
                resumeRepository,
                jobsRepository
        );

        var matches = service.findMatches(1L, 10, "India", "AMAZON");

        assertEquals(1, matches.size());
        assertEquals("AMAZON:1", matches.get(0).externalId());
    }
}
