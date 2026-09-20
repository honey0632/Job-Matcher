package com.honey.jobfetcher.provider;

import java.util.regex.Pattern;

public class JobLocationMatcher {

    private static final Pattern INDIA_PATTERN = Pattern.compile("\\b(IND|IN|INDIA|INDIAN)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern USA_PATTERN = Pattern.compile("\\b(USA|US|UNITED STATES|AMERICA)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern REMOTE_PATTERN = Pattern.compile("\\bREMOTE\\b", Pattern.CASE_INSENSITIVE);

    public static boolean matchesLocation(String location, String targetCountry) {
        if (location == null || location.isBlank()) {
            return false;
        }

        if (REMOTE_PATTERN.matcher(location).find()) {
            return true;
        }

        return switch (targetCountry.toUpperCase()) {
            case "INDIA" -> INDIA_PATTERN.matcher(location).find();
            case "USA" -> USA_PATTERN.matcher(location).find();
            default -> location.toLowerCase().contains(targetCountry.toLowerCase());
        };
    }
}