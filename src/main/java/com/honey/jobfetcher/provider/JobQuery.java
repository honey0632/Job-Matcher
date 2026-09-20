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
}