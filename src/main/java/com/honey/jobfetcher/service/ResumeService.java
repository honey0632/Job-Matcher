package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.ResumeResponse;
import com.honey.jobfetcher.exception.InvalidResumeException;
import com.honey.jobfetcher.exception.ResumeExtractionException;
import com.honey.jobfetcher.exception.ResumeStorageException;
import com.honey.jobfetcher.extractor.ResumeTextExtractor;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.model.User;
import com.honey.jobfetcher.repository.ResumeRepository;
import com.honey.jobfetcher.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ResumeService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx");

    private final ResumeRepository resumeRepository;
    private final ResumeTextExtractor resumeTextExtractor;
    private final Path storageDirectory;

    public ResumeService(
            ResumeRepository resumeRepository,
            ResumeTextExtractor resumeTextExtractor,
            @Value("${app.resume.storage-dir:./data/resumes}") String storageDirectory
    ) {
        this.resumeRepository = resumeRepository;
        this.resumeTextExtractor = resumeTextExtractor;
        this.storageDirectory = Paths.get(storageDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException exception) {
            throw new ResumeStorageException(
                    "Unable to create resume storage directory",
                    exception
            );
        }
    }

    public ResumeResponse upload(User user, MultipartFile file) {
        validateFile(file);
        Long userId = user.getId();

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );
        String extension = extensionOf(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path userDirectory = storageDirectory.resolve(userId.toString()).normalize();
        Path destination = userDirectory.resolve(storedFilename).normalize();

        if (!destination.startsWith(userDirectory)) {
            throw new InvalidResumeException("Invalid resume filename");
        }

        try {
            Files.createDirectories(userDirectory);
            Files.copy(file.getInputStream(), destination);
        } catch (IOException exception) {
            throw new ResumeStorageException("Unable to store uploaded resume", exception);
        }

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setOriginalFilename(originalFilename);
        resume.setStoredFilename(userId + "/" + storedFilename);
        resume.setContentType(file.getContentType());
        resume.setFileSize(file.getSize());
        resume.setUploadedAt(LocalDateTime.now());

        try {
            resume.setExtractedText(
                    resumeTextExtractor.extract(destination, file.getContentType())
            );
            resume.setStatus("EXTRACTED");
            resume.setExtractedAt(LocalDateTime.now());
        } catch (IOException | RuntimeException exception) {
            resume.setStatus("EXTRACTION_FAILED");
            resumeRepository.save(resume);
            throw new ResumeExtractionException(
                    "Unable to extract text from uploaded resume",
                    exception
            );
        }

        return ResumeResponse.from(resumeRepository.save(resume));
    }

    public List<ResumeResponse> findByUser(User user) {
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(ResumeResponse::from)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidResumeException("Resume file is required");
        }

        String filename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );
        String extension = extensionOf(filename);
        String contentType = file.getContentType();

        boolean validPdf = "pdf".equals(extension) && PDF_CONTENT_TYPE.equals(contentType);
        boolean validDocx = "docx".equals(extension) && DOCX_CONTENT_TYPE.equals(contentType);

        if (!validPdf && !validDocx) {
            throw new InvalidResumeException("Only PDF and DOCX resumes are supported");
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new InvalidResumeException("Resume must have a PDF or DOCX extension");
        }

        String extension = filename.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidResumeException("Only PDF and DOCX resumes are supported");
        }
        return extension;
    }
}
