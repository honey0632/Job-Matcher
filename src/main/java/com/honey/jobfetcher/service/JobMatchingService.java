package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;

import java.util.List;

public interface JobMatchingService {

    List<JobMatchResponse> findMatches(Long resumeId, int limit);
}
