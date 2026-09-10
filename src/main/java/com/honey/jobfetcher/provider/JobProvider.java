package com.honey.jobfetcher.provider;

import com.honey.jobfetcher.model.Jobs;

import java.util.List;

/**
 * Retrieves normalized jobs from one approved source.
 */
public interface JobProvider {
    JobSource source();

    List<Jobs> fetchJobs(String query);
}
