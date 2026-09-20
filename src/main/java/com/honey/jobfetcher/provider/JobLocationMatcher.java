package com.honey.jobfetcher.provider;

import java.util.regex.Pattern;

public class JobLocationMatcher {

    private static final Pattern INDIA_PATTERN = Pattern.compile("\\b(IND|IN|INDIA|INDIAN)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern USA_PATTERN = Pattern.compile("\\b(USA|US|UNITED STATES|AMERICA)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern US_STATE_PATTERN = Pattern.compile(
            "\\b(AL|AK|AZ|AR|CA|CO|CT|DE|FL|GA|HI|ID|IL|IA|KS|KY|LA|ME|MD|MA|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI|WY)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern REMOTE_PATTERN = Pattern.compile("\\bREMOTE\\b", Pattern.CASE_INSENSITIVE);

    public static boolean matches(String location, String targetCountry) {
        if (location != null && location.toLowerCase().contains("work from home")) {
            return true;
        }
        return matchesLocation(location, targetCountry);
    }

    public static boolean matchesLocation(String location, String targetCountry) {
        if (location == null || location.isBlank()) {
            return false;
        }

        if (REMOTE_PATTERN.matcher(location).find()) {
            return true;
        }

        return switch (targetCountry == null ? "" : targetCountry.toUpperCase()) {
            case "INDIA" -> INDIA_PATTERN.matcher(location).find();
            case "USA", "UNITED STATES" ->
                    USA_PATTERN.matcher(location).find() || US_STATE_PATTERN.matcher(location).find();
            default -> targetCountry != null
                    && location.toLowerCase().contains(targetCountry.toLowerCase());
        };
    }
}