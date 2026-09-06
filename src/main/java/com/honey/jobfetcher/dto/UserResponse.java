package com.honey.jobfetcher.dto;

import com.honey.jobfetcher.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt()
        );
    }
}
