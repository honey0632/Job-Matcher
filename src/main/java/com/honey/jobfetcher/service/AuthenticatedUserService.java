// Resolves the currently authenticated application user.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthAuthentication)) {
            throw new IllegalStateException("An OAuth2-authenticated user is required");
        }

        String provider = oauthAuthentication.getAuthorizedClientRegistrationId();
        String subject = authentication.getName();

        return userRepository.findByOauthProviderAndOauthSubject(provider, subject)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user account was not found"
                ));
    }
}
