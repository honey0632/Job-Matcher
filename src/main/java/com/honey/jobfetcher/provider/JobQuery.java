package com.honey.jobfetcher.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validates user-controlled source-search input and derives matching terms.
 */
public final class JobQuery {
    private static final int MAX_QUERY_LENGTH = 200;
    private static final Set<String> IGNORED_TERMS = Set.of(
            "and", "for", "from", "india", "job", "jobs", "of", "remote",
            "the", "with", "year", "years", "experience"
    );

    private JobQuery() {
    }

    public static String requireValid(String query) {
        if (query == null) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        String normalized = query.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank() || normalized.length() > MAX_QUERY_LENGTH
                || normalized.indexOf('\r') >= 0 || normalized.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Search query must be between 1 and 200 characters");
        }
        return normalized;
    }

    public static List<String> meaningfulTerms(String query) {
        String normalized = requireValid(query).toLowerCase(Locale.ROOT);
        return Arrays.stream(normalized.split("[^\\p{Alnum}]+"))
                .filter(term -> term.length() >= 3 && !IGNORED_TERMS.contains(term))
                .distinct()
                .toList();
    }

    public static boolean matchesText(String query, String... values) {
        List<String> terms = meaningfulTerms(query);
        if (terms.isEmpty()) {
            return false;
        }

        String searchableText = Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "))
                .toLowerCase(Locale.ROOT);
        return terms.stream().anyMatch(searchableText::contains);
    }

    public static boolean isIndiaSearch(String query) {
        return requireValid(query).toLowerCase(Locale.ROOT).matches(".*\\bindia\\b.*");
    }
}
