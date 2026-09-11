// Tests HTTP behavior exposed by the jobs controller.

package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.service.AuthenticatedUserService;
import com.honey.jobfetcher.service.CriteriaSearchService;
import com.honey.jobfetcher.service.JobsService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class JobsControllerTest {

    private final JobsService jobsService = mock(JobsService.class);
    private final MockMvc mockMvc = standaloneSetup(new JobsController(
            jobsService,
            mock(AuthenticatedUserService.class),
            mock(CriteriaSearchService.class),
            mock(com.honey.jobfetcher.service.SavedJobService.class)
    )).build();

    @Test
    void returnsJobsFromSearchEndpoint() throws Exception {
        Jobs job = new Jobs();
        job.setExternalId("external-1");
        job.setTitle("Software Engineer");
        job.setCompany("Google");

        when(jobsService.fetchAndSaveGoogleJobs("java")).thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs/search").param("query", "java"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"externalId":"external-1","title":"Software Engineer","company":"Google"}]
                        """));
    }
}
