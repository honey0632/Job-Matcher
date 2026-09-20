package com.honey.jobfetcher.controller;

import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.service.AccountService;
import com.honey.jobfetcher.service.OAuth2AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final OAuth2AccountService oauth2AccountService;

    public AuthController(AccountService accountService, OAuth2AccountService oauth2AccountService) {
        this.accountService = accountService;
        this.oauth2AccountService = oauth2AccountService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegistrationRequest request) {
        User user = accountService.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        User user = accountService.authenticate(request.email(), request.password());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/oauth2")
    public ResponseEntity<User> oauth2Login(@AuthenticationPrincipal OAuth2User oauth2User) {
        User user = oauth2AccountService.upsertOAuth2User(oauth2User, "google");
        return ResponseEntity.ok(user);
    }

    public record RegistrationRequest(
        String email,
        String password,
        String displayName
    ) {}

    public record LoginRequest(
        String email,
        String password
    ) {}
}