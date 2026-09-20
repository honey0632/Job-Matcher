package com.honey.jobfetcher.service;

import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OAuth2AccountService {

    private final UserRepository userRepository;

    public OAuth2AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User upsertOAuth2User(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute("email");
        String subject = oauth2User.getName();
        String displayName = oauth2User.getAttribute("name");
        return upsertAccount(provider, subject, email, displayName);
    }

    public User upsertAccount(String provider, String subject, String email, String displayName) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setCreatedAt(LocalDateTime.now());
                return newUser;
            });

        user.setOauthProvider(provider);
        user.setOauthSubject(subject);
        user.setDisplayName(displayName);

        return userRepository.save(user);
    }
}