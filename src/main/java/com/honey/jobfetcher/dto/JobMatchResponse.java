// Represents a scored job match returned by the API.

package com.honey.jobfetcher.dto;

import com.honey.jobfetcher.model.Jobs;

public record JobMatchResponse(
        Long jobId,
        String externalId,
        String title,
        String company,
        String location,
        String description,
        String jobUrl,
        int score
) {
    public static JobMatchResponse from(Jobs job, int score) {
        return new JobMatchResponse(
                job.getId(),
                job.getExternalId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                job.getJobUrl(),
                score
        );
    }
}
