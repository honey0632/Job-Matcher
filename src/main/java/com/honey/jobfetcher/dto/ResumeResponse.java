// Represents resume metadata returned by the API.

package com.honey.jobfetcher.dto;

import com.honey.jobfetcher.model.Resume;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        Long userId,
        String originalFilename,
        String contentType,
        long fileSize,
        String status,
        LocalDateTime uploadedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getUser().getId(),
                resume.getOriginalFilename(),
                resume.getContentType(),
                resume.getFileSize(),
                resume.getStatus(),
                resume.getUploadedAt()
        );
    }
}
