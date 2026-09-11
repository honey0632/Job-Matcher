package com.honey.jobfetcher.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobLocationMatcherTest {

    @Test
    void matchesCountrySynonymsAndTokens() {
        // India synonyms
        assertTrue(JobLocationMatcher.matches("Bengaluru, KA, IND", "India"));
        assertTrue(JobLocationMatcher.matches("Bengaluru, KA, 560001, IN", "India"));
        assertTrue(JobLocationMatcher.matches("Hyderabad, Telangana, India", "India"));
        assertTrue(JobLocationMatcher.matches("Remote, India", "India"));
        assertTrue(JobLocationMatcher.matches("Work From Home", ""));
        assertFalse(JobLocationMatcher.matches("Seattle, WA, USA", "India"));

        // US synonyms
        assertTrue(JobLocationMatcher.matches("Seattle, WA, USA", "United States"));
        assertTrue(JobLocationMatcher.matches("Austin, TX, US", "United States"));
        assertTrue(JobLocationMatcher.matches("Santa Clara, CA", "United States"));
        assertFalse(JobLocationMatcher.matches("Bengaluru, KA, IND", "United States"));

        // Remote matching
        assertTrue(JobLocationMatcher.matches("Remote - Anywhere", "India"));
        assertTrue(JobLocationMatcher.matches("Fully Remote", "United States"));
    }
}
