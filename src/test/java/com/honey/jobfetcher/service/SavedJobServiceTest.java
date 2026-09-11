package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.model.SavedJob;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.JobsRepository;
import com.honey.jobfetcher.repository.SavedJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private JobsRepository jobsRepository;

    @InjectMocks
    private SavedJobService savedJobService;

    @Test
    void autoSavesMatchesWithScoreGreaterThanEightyPercent() {
        User user = new User();
        user.setId(1L);

        Jobs highJob = new Jobs();
        highJob.setId(10L);

        Jobs lowJob = new Jobs();
        lowJob.setId(20L);

        when(jobsRepository.findById(10L)).thenReturn(Optional.of(highJob));
        when(savedJobRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());
        when(savedJobRepository.save(any(SavedJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobMatchResponse highMatch = new JobMatchResponse(10L, "ext-10", "Dev", "Google", "India", "Desc", "url", "GOOGLE", 85);
        JobMatchResponse lowMatch = new JobMatchResponse(20L, "ext-20", "Dev", "Google", "India", "Desc", "url", "GOOGLE", 75);

        savedJobService.autoSaveHighMatches(user, List.of(highMatch, lowMatch));

        verify(jobsRepository).findById(10L);
        verify(jobsRepository, never()).findById(20L);
        verify(savedJobRepository).save(any(SavedJob.class));
    }
}
