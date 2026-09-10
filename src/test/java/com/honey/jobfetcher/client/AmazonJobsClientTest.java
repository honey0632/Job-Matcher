package com.honey.jobfetcher.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmazonJobsClientTest {
    @Mock
    private SourceHttpClient sourceHttpClient;

    @Test
    void requestsBoundedIndiaSearchUsingDocumentedParameters() {
        when(sourceHttpClient.get(any(URI.class), eq("Amazon Jobs"))).thenReturn("{\"jobs\":[]}");
        AmazonJobsClient client = new AmazonJobsClient(sourceHttpClient);

        client.fetchSearchPage("Java Engineer India", 100, 100);

        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        verify(sourceHttpClient).get(uri.capture(), eq("Amazon Jobs"));
        assertEqualsEndpoint(uri.getValue());
        assertTrue(uri.getValue().getRawQuery().contains("base_query=Java%20Engineer%20India"));
        assertTrue(uri.getValue().getRawQuery().contains("loc_query=India"));
        assertTrue(uri.getValue().getRawQuery().contains("offset=100"));
        assertTrue(uri.getValue().getRawQuery().contains("result_limit=100"));
        assertTrue(uri.getValue().getRawQuery().contains("sort=recent"));
    }

    @Test
    void rejectsUnboundedResultLimits() {
        AmazonJobsClient client = new AmazonJobsClient(sourceHttpClient);

        assertThrows(IllegalArgumentException.class, () -> client.fetchSearchPage("Java", 0, 101));
    }

    private void assertEqualsEndpoint(URI uri) {
        org.junit.jupiter.api.Assertions.assertEquals("https", uri.getScheme());
        org.junit.jupiter.api.Assertions.assertEquals("www.amazon.jobs", uri.getHost());
        org.junit.jupiter.api.Assertions.assertEquals("/en/search.json", uri.getPath());
    }
}
