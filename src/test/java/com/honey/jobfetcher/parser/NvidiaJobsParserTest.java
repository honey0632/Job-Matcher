package com.honey.jobfetcher.parser;

import com.honey.jobfetcher.model.Jobs;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NvidiaJobsParserTest {
    private final NvidiaJobsParser parser = new NvidiaJobsParser();

    @Test
    void retainsOnlyAllowlistedSitemapPagesAndMapsJobPostingJsonLd() {
        List<URI> pages = parser.parseSitemap("""
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                  <url><loc>https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite/job/India/Engineer_123</loc></url>
                  <url><loc>https://example.test/job/untrusted</loc></url>
                </urlset>
                """);

        assertEquals(List.of(URI.create(
                "https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite/job/India/Engineer_123"
        )), pages);

        Jobs job = parser.parseJobPage("""
                <html><head><script type="application/ld+json">
                {"@context":"https://schema.org","@type":"JobPosting",
                 "identifier":{"value":"JR-42"},"title":"GPU Software Engineer",
                 "description":"<p>Develop <strong>GPU</strong> software.</p>",
                 "url":"/NVIDIAExternalCareerSite/job/India/Engineer_123",
                 "jobLocation":{"address":{"addressLocality":"Bengaluru",
                   "addressRegion":"KA","postalCode":"560001","addressCountry":"IN"}}}
                </script></head></html>
                """, pages.getFirst());

        assertEquals("NVIDIA:JR-42", job.getExternalId());
        assertEquals("Develop GPU software.", job.getDescription());
        assertEquals("Bengaluru, KA, 560001, IN", job.getLocation());
        assertEquals(pages.getFirst().toString(), job.getJobUrl());
        assertEquals("NVIDIA", job.getCompany());
        assertEquals("NVIDIA", job.getSource());
    }

    @Test
    void rejectsJobPostingUrlsOutsideThePublicAllowlist() {
        assertThrows(IllegalStateException.class, () -> parser.parseJobPage("""
                <script type="application/ld+json">
                {"@type":"JobPosting","identifier":{"value":"JR-42"},"title":"Engineer",
                 "url":"https://example.test/job/JR-42"}
                </script>
                """, URI.create("https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite/job/India/Engineer_123")));
    }

    @Test
    void rejectsEncodedPathTraversalInSitemapUrls() {
        assertEquals(List.of(), parser.parseSitemap("""
                <urlset><url><loc>https://nvidia.wd5.myworkdayjobs.com/NVIDIAExternalCareerSite/job/%2e%2e/siteMap.xml</loc></url></urlset>
                """));
    }
}
