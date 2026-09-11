// Scores jobs locally using keyword overlap with a resume.

package com.honey.jobfetcher.service;

import com.honey.jobfetcher.dto.JobMatchResponse;
import com.honey.jobfetcher.exception.InvalidResumeException;
import com.honey.jobfetcher.model.Jobs;
import com.honey.jobfetcher.model.Resume;
import com.honey.jobfetcher.provider.JobLocationMatcher;
import com.honey.jobfetcher.repository.JobsRepository;
import com.honey.jobfetcher.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "app.matching.provider", havingValue = "keyword", matchIfMissing = true)
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
    public List<JobMatchResponse> findMatches(Long resumeId, int limit, String location) {
        return findMatches(resumeId, limit, location, null);
    }

    @Override
    public List<JobMatchResponse> findMatches(Long resumeId, int limit, String location, String source) {
        // The keyword provider remains available as a local/offline fallback.
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
                .filter(job -> {
                    if (source == null || source.isBlank() || "ALL".equalsIgnoreCase(source)) {
                        return true;
                    }
                    return matchesSource(job, source.trim().toLowerCase(Locale.ROOT));
                })
                .filter(job -> JobLocationMatcher.matches(job.getLocation(), location))
                .map(job -> match(job, resumeWords))
                .filter(match -> match.score() >= 0)
                .sorted(Comparator.comparingInt(JobMatchResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    private boolean matchesSource(Jobs job, String targetSource) {
        if (job.getSource() != null && job.getSource().toLowerCase(Locale.ROOT).contains(targetSource)) {
            return true;
        }
        if (job.getCompany() != null && job.getCompany().toLowerCase(Locale.ROOT).contains(targetSource)) {
            return true;
        }
        if (job.getExternalId() != null && job.getExternalId().toLowerCase(Locale.ROOT).startsWith(targetSource + ":")) {
            return true;
        }
        if ("google_careers".equals(targetSource) || "google".equals(targetSource)) {
            return job.getCompany() != null && job.getCompany().toLowerCase(Locale.ROOT).contains("google");
        }
        if ("wells_fargo".equals(targetSource) || "wells".equals(targetSource)) {
            return job.getCompany() != null && job.getCompany().toLowerCase(Locale.ROOT).contains("wells");
        }
        return false;
    }

    private boolean matchesLocation(Jobs job, String location) {
        return JobLocationMatcher.matches(job.getLocation(), location);
    }

    private JobMatchResponse match(Jobs job, Set<String> resumeWords) {
        Set<String> titleWords = keywords(job.getTitle() == null ? "" : job.getTitle());
        Set<String> jobWords = keywords(
                (job.getTitle() == null ? "" : job.getTitle()) + " "
                        + (job.getDescription() == null ? "" : job.getDescription())
        );

        if (jobWords.isEmpty()) {
            return JobMatchResponse.from(job, 0);
        }

        long matchingJobWords = jobWords.stream()
                .filter(resumeWords::contains)
                .count();

        long matchingTitleWords = titleWords.stream()
                .filter(resumeWords::contains)
                .count();

        double baseRatio = (matchingJobWords * 100.0) / Math.min(jobWords.size(), Math.max(10, resumeWords.size()));

        if (!titleWords.isEmpty() && matchingTitleWords > 0) {
            double titleRatio = (matchingTitleWords * 100.0) / titleWords.size();
            baseRatio = Math.max(baseRatio, titleRatio * 0.85 + baseRatio * 0.15);
        }

        int score = (int) Math.round(baseRatio);
        return JobMatchResponse.from(job, Math.min(100, Math.max(0, score)));
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
