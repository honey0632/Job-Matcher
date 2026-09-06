package com.honey.jobfetcher.dto;

import com.honey.jobfetcher.model.SearchPreference;

public record SearchPreferenceResponse(
        String country,
        Integer experienceYears,
        String desiredRole
) {
    public static SearchPreferenceResponse from(SearchPreference preference) {
        return new SearchPreferenceResponse(
                preference.getCountry(),
                preference.getExperienceYears(),
                preference.getDesiredRole()
        );
    }
}
