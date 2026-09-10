package com.honey.jobfetcher.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Selects which approved providers take part in criteria searches.
 */
@Component
@ConfigurationProperties(prefix = "app.job-sources")
public class ApprovedJobSourcesProperties {
    private List<String> enabled = new ArrayList<>(List.of(
            JobSource.GOOGLE_CAREERS.name(),
            JobSource.AMAZON.name(),
            JobSource.WELLS_FARGO.name(),
            JobSource.NVIDIA.name()
    ));

    public List<String> getEnabled() {
        return enabled;
    }

    public void setEnabled(List<String> enabled) {
        this.enabled = enabled == null ? List.of() : new ArrayList<>(enabled);
    }

    public List<JobSource> enabledSources() {
        return enabled.stream()
                .map(source -> {
                    try {
                        return JobSource.valueOf(source.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException | NullPointerException exception) {
                        throw new IllegalStateException("Unsupported app.job-sources.enabled value: " + source, exception);
                    }
                })
                .distinct()
                .toList();
    }
}
