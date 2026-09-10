// Verifies job retrieval and search service behavior.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.client.GoogleCareersClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.GoogleCareersParser;
import com.honey.jobfetcher.provider.ApprovedJobSourcesProperties;
import com.honey.jobfetcher.provider.JobProvider;
import com.honey.jobfetcher.provider.JobSource;
import com.honey.jobfetcher.repository.JobsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobsServiceTest {

    @Mock
    private JobsRepository jobsRepository;

    @Mock
    private GoogleCareersClient googleCareersClient;

    @Mock
    private GoogleCareersParser googleCareersParser;

    @Test
    void savesNewJobsWithTimestamps() {
        Jobs fetchedJob = job("external-1", "New job");

        when(googleCareersClient.fetchSearchPage("java")).thenReturn("html");
        when(googleCareersParser.parse("html")).thenReturn(List.of(fetchedJob));
        when(jobsRepository.findByExternalId("external-1")).thenReturn(Optional.empty());
        when(jobsRepository.save(any(Jobs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobsService service = new JobsService(
                jobsRepository,
                googleCareersClient,
                googleCareersParser
        );

        List<Jobs> savedJobs = service.fetchAndSaveGoogleJobs("java");

        assertEquals(1, savedJobs.size());
        ArgumentCaptor<Jobs> captor = ArgumentCaptor.forClass(Jobs.class);
        verify(jobsRepository).save(captor.capture());
        assertEquals("external-1", captor.getValue().getExternalId());
        assertEquals("New job", captor.getValue().getTitle());
        org.junit.jupiter.api.Assertions.assertNotNull(captor.getValue().getCreatedAt());
        org.junit.jupiter.api.Assertions.assertNotNull(captor.getValue().getUpdatedAt());
    }

    @Test
    void updatesExistingJobWithoutChangingApplicationStatus() {
        Jobs existingJob = job("external-1", "Old title");
        existingJob.setStatus(Jobs.STATUS_APPLIED);
        Jobs fetchedJob = job("external-1", "Updated title");

        when(googleCareersClient.fetchSearchPage("java")).thenReturn("html");
        when(googleCareersParser.parse("html")).thenReturn(List.of(fetchedJob));
        when(jobsRepository.findByExternalId("external-1")).thenReturn(Optional.of(existingJob));
        when(jobsRepository.save(any(Jobs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobsService service = new JobsService(
                jobsRepository,
                googleCareersClient,
                googleCareersParser
        );

        service.fetchAndSaveGoogleJobs("java");

        assertEquals("Updated title", existingJob.getTitle());
        assertEquals(Jobs.STATUS_APPLIED, existingJob.getStatus());
        verify(jobsRepository).save(existingJob);
        verify(jobsRepository, never()).save(fetchedJob);
    }

    @Test
    void savesJobsFromEveryConfiguredApprovedProvider() {
        JobProvider google = mock(JobProvider.class);
        JobProvider amazon = mock(JobProvider.class);
        Jobs googleJob = job("google-1", "Google job");
        Jobs amazonJob = job("AMAZON:amazon-1", "Amazon job");

        when(google.source()).thenReturn(JobSource.GOOGLE_CAREERS);
        when(amazon.source()).thenReturn(JobSource.AMAZON);
        when(google.fetchJobs("java")).thenReturn(List.of(googleJob));
        when(amazon.fetchJobs("java")).thenReturn(List.of(amazonJob));
        when(jobsRepository.findByExternalId("google-1")).thenReturn(Optional.empty());
        when(jobsRepository.findByExternalId("AMAZON:amazon-1")).thenReturn(Optional.empty());
        when(jobsRepository.save(any(Jobs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovedJobSourcesProperties sources = new ApprovedJobSourcesProperties();
        sources.setEnabled(List.of("GOOGLE_CAREERS", "AMAZON"));
        JobsService service = new JobsService(jobsRepository, List.of(google, amazon), sources);

        List<Jobs> saved = service.fetchAndSaveApprovedJobs("java");

        assertEquals(List.of(googleJob, amazonJob), saved);
        verify(google).fetchJobs("java");
        verify(amazon).fetchJobs("java");
    }

    @Test
    void identifiesTheFailingApprovedProvider() {
        JobProvider amazon = mock(JobProvider.class);
        when(amazon.source()).thenReturn(JobSource.AMAZON);
        when(amazon.fetchJobs("java")).thenThrow(new IllegalStateException("upstream timeout"));
        ApprovedJobSourcesProperties sources = new ApprovedJobSourcesProperties();
        sources.setEnabled(List.of("AMAZON"));
        JobsService service = new JobsService(jobsRepository, List.of(amazon), sources);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.fetchAndSaveApprovedJobs("java")
        );

        assertEquals("Failed to fetch AMAZON jobs", exception.getMessage());
    }

    private Jobs job(String externalId, String title) {
        Jobs job = new Jobs();
        job.setExternalId(externalId);
        job.setTitle(title);
        job.setCompany("Google");
        job.setJobUrl("https://www.google.com/jobs/" + externalId);
        job.setSource("GOOGLE_CAREERS");
        return job;
    }
}
