package com.honey.jobfetcher.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@ConfigurationProperties(prefix = "app.job-sources")
public class ApprovedJobSourcesProperties {
    private List<String> enabled;

    public List<JobSource> enabledSources() {
        if (enabled == null || enabled.isEmpty()) {
            return Arrays.asList(
                JobSource.GOOGLE_CAREERS.name(),
                JobSource.AMAZON.name(),
                JobSource.WELLS_FARGO.name(),
                JobSource.NVIDIA.name(),
                JobSource.ADOBE.name(),
                JobSource.META.name(),
                JobSource.APPLE.name(),
                JobSource.UBER.name(),
                JobSource.INFOSYS.name(),
                JobSource.WIPRO.name(),
                JobSource.SALESFORCE.name(),
                JobSource.ATLASSIAN.name()
            ).stream()
                .map(JobSource::valueOf)
                .toList();
        }

        return enabled.stream()
            .map(source -> JobSource.valueOf(source.trim().toUpperCase(Locale.ROOT)))
            .toList();
    }

    public void setEnabled(List<String> enabled) {
        this.enabled = enabled;
    }
}