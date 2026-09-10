package com.honey.jobfetcher.client;

import com.honey.jobfetcher.provider.JobUrlPolicy;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Retrieves only NVIDIA's public sitemap and allowlisted public job pages.
 */
@Component
public class NvidiaJobsClient {
    static final int MAX_SITEMAP_CHARACTERS = 3_000_000;
    static final int MAX_JOB_PAGE_CHARACTERS = 1_000_000;
    static final URI SITEMAP_URI = URI.create(
            "https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite/siteMap.xml"
    );
    public static final String NVIDIA_HOST = "nvidia.wd5.myworkdayjobs.com";
    public static final String[] JOB_PATH_PREFIXES = {
            "/NVIDIAExternalCareerSite/job/",
            "/en-US/NVIDIAExternalCareerSite/job/"
    };

    private final SourceHttpClient sourceHttpClient;

    public NvidiaJobsClient(SourceHttpClient sourceHttpClient) {
        this.sourceHttpClient = sourceHttpClient;
    }

    public String fetchSitemap() {
        String response = sourceHttpClient.get(SITEMAP_URI, "NVIDIA sitemap");
        if (response.length() > MAX_SITEMAP_CHARACTERS) {
            throw new IllegalStateException("NVIDIA sitemap exceeds the 3 MB safety limit");
        }
        return response;
    }

    public String fetchPublicJobPage(URI jobPage) {
        URI canonicalPage = canonicalPublicJobPage(jobPage);
        String response = sourceHttpClient.get(canonicalPage, "NVIDIA job page");
        if (response.length() > MAX_JOB_PAGE_CHARACTERS) {
            throw new IllegalStateException("NVIDIA job page exceeds the 1 MB safety limit");
        }
        return response;
    }

    public static URI canonicalPublicJobPage(URI jobPage) {
        return JobUrlPolicy.canonicalize(
                jobPage == null ? null : jobPage.toString(),
                SITEMAP_URI,
                NVIDIA_HOST,
                JOB_PATH_PREFIXES
        ).orElseThrow(() -> new IllegalArgumentException("NVIDIA job page URL is outside the public allowlist"));
    }
}
