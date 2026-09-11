// Carries criteria for searching and filtering jobs.

package com.honey.jobfetcher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobSearchRequest(
        @NotBlank(message = "Country is required")
        String country,
        @Min(value = 0, message = "Experience cannot be negative")
        Integer experienceYears,
        @NotBlank(message = "Desired role is required")
        String desiredRole,
        String source
) {
    public JobSearchRequest(String country, Integer experienceYears, String desiredRole) {
        this(country, experienceYears, desiredRole, null);
    }
}
