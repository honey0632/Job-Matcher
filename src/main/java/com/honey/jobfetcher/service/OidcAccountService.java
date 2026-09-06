package com.honey.jobfetcher.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class OidcAccountService extends OidcUserService {

    private final OAuth2AccountService accountService;

    public OidcAccountService(OAuth2AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        accountService.upsertAccount(
                provider,
                oidcUser.getSubject(),
                oidcUser.getEmail(),
                oidcUser.getFullName()
        );
        return oidcUser;
    }
}
