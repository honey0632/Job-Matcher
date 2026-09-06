package com.honey.jobfetcher.repository;

import com.honey.jobfetcher.model.SearchPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchPreferenceRepository extends JpaRepository<SearchPreference, Long> {
    Optional<SearchPreference> findByUserId(Long userId);
}
