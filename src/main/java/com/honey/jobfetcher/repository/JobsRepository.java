// Provides persistence operations for job listings.

package com.honey.jobfetcher.repository;

import com.honey.jobfetcher.model.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobsRepository extends JpaRepository<Jobs,Long> {
    Optional<Jobs> findByExternalId(String externalId);
}
