// Defines the contract for resume-to-job matching providers.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;

import java.util.List;

public interface JobMatchingService {

    /**
     * Scores resume/job matches while applying the requested location filter.
     */
    List<JobMatchResponse> findMatches(Long resumeId, int limit, String location);

    /**
     * Scores resume/job matches while applying requested location and source filters.
     */
    default List<JobMatchResponse> findMatches(Long resumeId, int limit, String location, String source) {
        return findMatches(resumeId, limit, location);
    }
}
