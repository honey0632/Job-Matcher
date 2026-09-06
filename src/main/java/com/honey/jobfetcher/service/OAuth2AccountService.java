package com.honey.jobfetcher.service;

import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OAuth2AccountService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauthUser = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String subject = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        upsertAccount(provider, subject, email, name);

        return oauthUser;
    }

    public void upsertAccount(String provider, String subject, String email, String name) {
        if (subject == null || email == null) {
            throw new IllegalStateException("OAuth provider did not return subject and email");
        }

        User user = userRepository
                .findByOauthProviderAndOauthSubject(provider, subject)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(User::new));

        user.setEmail(email);
        user.setOauthProvider(provider);
        user.setOauthSubject(subject);
        user.setDisplayName(name);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        userRepository.save(user);
    }
}
