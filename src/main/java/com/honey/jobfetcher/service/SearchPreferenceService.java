// Manages users’ saved job-search preferences.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.SearchPreferenceRequest;
import com.honey.jobfetcher.dto.SearchPreferenceResponse;
import com.honey.jobfetcher.model.SearchPreference;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.SearchPreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class SearchPreferenceService {

    private final SearchPreferenceRepository preferenceRepository;

    public SearchPreferenceService(SearchPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    public SearchPreferenceResponse get(User user) {
        return preferenceRepository.findByUserId(user.getId())
                .map(SearchPreferenceResponse::from)
                .orElse(null);
    }

    public SearchPreferenceResponse save(
            User user,
            SearchPreferenceRequest request
    ) {
        SearchPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElseGet(SearchPreference::new);

        preference.setUser(user);
        preference.setCountry(request.country().trim());
        preference.setExperienceYears(request.experienceYears());
        preference.setDesiredRole(request.desiredRole().trim());

        return SearchPreferenceResponse.from(preferenceRepository.save(preference));
    }
}
