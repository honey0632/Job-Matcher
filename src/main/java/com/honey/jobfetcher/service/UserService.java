package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.CreateUserRequest;
import com.honey.jobfetcher.dto.UserResponse;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setCreatedAt(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        return UserResponse.from(user);
    }
}
