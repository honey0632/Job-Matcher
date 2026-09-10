package com.honey.jobfetcher.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NvidiaJobsClientTest {
    @Mock
    private SourceHttpClient sourceHttpClient;

    @Test
    void refusesToFetchUrlsOutsideNvidiasImmutablePublicJobPath() {
        NvidiaJobsClient client = new NvidiaJobsClient(sourceHttpClient);

        assertThrows(
                IllegalArgumentException.class,
                () -> client.fetchPublicJobPage(URI.create("https://example.test/job/untrusted"))
        );
        verifyNoInteractions(sourceHttpClient);
    }
}
