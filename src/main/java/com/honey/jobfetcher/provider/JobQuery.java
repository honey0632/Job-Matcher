package com.honey.jobfetcher.provider;

public class JobQuery {

    public static String requireValid(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }
        return query.trim();
    }

    public static boolean isIndiaSearch(String query) {
        return query.toLowerCase().contains("india")
            || query.toLowerCase().contains("indian");
    }

    public static boolean matchesText(String query, String... values) {
        String[] terms = requireValid(query).toLowerCase().split("\\s+");
        String searchableText = java.util.Arrays.stream(values)
            .filter(java.util.Objects::nonNull)
            .map(String::toLowerCase)
            .reduce("", (left, right) -> left + " " + right);
        return java.util.Arrays.stream(terms)
            .filter(term -> term.length() > 2 && !term.matches("\\d+"))
            .anyMatch(searchableText::contains);
    }
}