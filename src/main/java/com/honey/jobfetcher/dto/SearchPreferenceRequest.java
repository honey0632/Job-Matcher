// Carries user-provided job search preferences.

package com.honey.jobfetcher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchPreferenceRequest(
        @NotBlank(message = "Country is required")
        String country,
        @Min(value = 0, message = "Experience cannot be negative")
        Integer experienceYears,
        @NotBlank(message = "Desired role is required")
        String desiredRole
) {
}
