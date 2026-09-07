// Provides persistence operations for users.

package com.honey.jobfetcher.repository;

import com.honey.jobfetcher.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthSubject(
            String oauthProvider,
            String oauthSubject
    );
}
