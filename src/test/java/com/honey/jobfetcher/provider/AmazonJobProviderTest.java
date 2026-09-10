package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.client.AmazonJobsClient;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.parser.AmazonJobsParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmazonJobProviderTest {
    @Mock
    private AmazonJobsClient client;

    @Mock
    private AmazonJobsParser parser;

    @Test
    void stopsWhenNextPageContainsOnlyPreviouslySeenIds() {
        List<Jobs> firstPage = IntStream.range(0, 100)
                .mapToObj(index -> job("AMAZON:" + index))
                .toList();
        when(client.fetchSearchPage("Engineer", 0, 100)).thenReturn("first");
        when(client.fetchSearchPage("Engineer", 100, 100)).thenReturn("repeated");
        when(parser.parse("first")).thenReturn(firstPage);
        when(parser.parse("repeated")).thenReturn(firstPage);

        List<Jobs> jobs = new AmazonJobProvider(client, parser).fetchJobs("Engineer");

        assertEquals(100, jobs.size());
        verify(client, times(2)).fetchSearchPage(eq("Engineer"), anyInt(), eq(100));
    }

    private Jobs job(String externalId) {
        Jobs job = new Jobs();
        job.setExternalId(externalId);
        return job;
    }
}
