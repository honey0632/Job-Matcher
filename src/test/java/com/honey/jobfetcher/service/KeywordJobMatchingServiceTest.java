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
        Resume resume = new Resume();
        resume.setExtractedText("Java Spring PostgreSQL backend engineer");

        Jobs matchingJob = new Jobs();
        matchingJob.setId(1L);
        matchingJob.setExternalId("job-1");
        matchingJob.setTitle("Java Spring Backend Engineer");
        matchingJob.setDescription("Build PostgreSQL services");
        matchingJob.setCompany("Google");

        Jobs unrelatedJob = new Jobs();
        unrelatedJob.setId(2L);
        unrelatedJob.setExternalId("job-2");
        unrelatedJob.setTitle("UX Designer");
        unrelatedJob.setDescription("Design user experiences");
        unrelatedJob.setCompany("Google");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(jobsRepository.findAll()).thenReturn(List.of(unrelatedJob, matchingJob));

        KeywordJobMatchingService service = new KeywordJobMatchingService(
                resumeRepository,
                jobsRepository
        );

        var matches = service.findMatches(1L, 10);

        assertEquals(1, matches.size());
        assertEquals("job-1", matches.get(0).externalId());
        assertTrue(matches.get(0).score() > 0);
    }
}
