package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.exception.InvalidResumeException;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.repository.JobsRepository;
import com.honey.jobfetcher.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class KeywordJobMatchingService implements JobMatchingService {

    private static final Set<String> STOP_WORDS = Set.of(
            "and", "the", "with", "for", "from", "that", "this",
            "have", "has", "are", "you", "your", "our", "will"
    );

    private final ResumeRepository resumeRepository;
    private final JobsRepository jobsRepository;

    public KeywordJobMatchingService(
            ResumeRepository resumeRepository,
            JobsRepository jobsRepository
    ) {
        this.resumeRepository = resumeRepository;
        this.jobsRepository = jobsRepository;
    }

    @Override
    public List<JobMatchResponse> findMatches(Long resumeId, int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Match limit must be between 1 and 100");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new InvalidResumeException(
                        "Resume not found with id: " + resumeId
                ));

        Set<String> resumeWords = keywords(resume.getExtractedText());
        if (resumeWords.isEmpty()) {
            return List.of();
        }

        return jobsRepository.findAll().stream()
                .map(job -> match(job, resumeWords))
                .filter(match -> match.score() > 0)
                .sorted(Comparator.comparingInt(JobMatchResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    private JobMatchResponse match(Jobs job, Set<String> resumeWords) {
        Set<String> jobWords = keywords(
                (job.getTitle() == null ? "" : job.getTitle()) + " "
                        + (job.getDescription() == null ? "" : job.getDescription())
        );

        long matchingWords = resumeWords.stream()
                .filter(jobWords::contains)
                .count();
        int score = (int) Math.round((matchingWords * 100.0) / resumeWords.size());
        return JobMatchResponse.from(job, Math.min(score, 100));
    }

    private Set<String> keywords(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return new HashSet<>(Arrays.stream(value.toLowerCase(Locale.ROOT)
                        .split("[^a-z0-9+#.-]+"))
                .filter(word -> word.length() >= 3)
                .filter(word -> !STOP_WORDS.contains(word))
                .toList());
    }
}
