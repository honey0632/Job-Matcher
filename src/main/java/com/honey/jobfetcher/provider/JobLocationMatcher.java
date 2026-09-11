package com.honey.jobfetcher.provider;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalizes and matches job locations against user country/location criteria.
 */
public final class JobLocationMatcher {

    private static final Map<String, Set<String>> COUNTRY_SYNONYMS = Map.ofEntries(
            Map.entry("india", Set.of("in", "ind", "india", "bengaluru", "bangalore", "hyderabad", "mumbai", "pune", "delhi", "noida", "gurgaon", "gurugram", "chennai", "kolkata")),
            Map.entry("united states", Set.of("us", "usa", "u.s.", "u.s.a.", "united states", "united states of america", "america", "seattle", "austin", "california", "new york", "texas", "washington", "san jose", "santa clara", "charlotte")),
            Map.entry("us", Set.of("us", "usa", "u.s.", "u.s.a.", "united states", "united states of america", "america", "seattle", "austin", "california", "new york", "texas", "washington", "san jose", "santa clara", "charlotte")),
            Map.entry("usa", Set.of("us", "usa", "u.s.", "u.s.a.", "united states", "united states of america", "america", "seattle", "austin", "california", "new york", "texas", "washington", "san jose", "santa clara", "charlotte")),
            Map.entry("united kingdom", Set.of("uk", "u.k.", "united kingdom", "great britain", "britain", "england", "london", "gb")),
            Map.entry("uk", Set.of("uk", "u.k.", "united kingdom", "great britain", "britain", "england", "london", "gb")),
            Map.entry("germany", Set.of("de", "germany", "deutschland", "berlin", "munich", "frankfurt")),
            Map.entry("canada", Set.of("ca", "canada", "toronto", "vancouver", "montreal", "ottawa")),
            Map.entry("australia", Set.of("au", "australia", "sydney", "melbourne", "brisbane")),
            Map.entry("singapore", Set.of("sg", "singapore")),
            Map.entry("ireland", Set.of("ie", "ireland", "dublin"))
    );

    private JobLocationMatcher() {
    }

    public static boolean matches(String jobLocation, String requestedLocation) {
        if (requestedLocation == null || requestedLocation.isBlank()) {
            return true;
        }
        if (jobLocation == null || jobLocation.isBlank()) {
            return false;
        }

        String normalizedJob = jobLocation.trim().toLowerCase(Locale.ROOT);
        String normalizedReq = requestedLocation.trim().toLowerCase(Locale.ROOT);

        if (normalizedJob.contains("remote") || normalizedJob.contains(normalizedReq) || normalizedReq.contains(normalizedJob)) {
            return true;
        }

        Set<String> synonyms = COUNTRY_SYNONYMS.get(normalizedReq);
        if (synonyms != null) {
            for (String synonym : synonyms) {
                if (synonym.length() <= 3) {
                    if (Pattern.compile("\\b" + Pattern.quote(synonym) + "\\b").matcher(normalizedJob).find()) {
                        return true;
                    }
                } else {
                    if (normalizedJob.contains(synonym)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
