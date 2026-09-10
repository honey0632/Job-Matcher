package com.honey.jobfetcher.provider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;

/**
 * Canonicalizes only HTTPS URLs belonging to an explicitly approved host and path.
 */
public final class JobUrlPolicy {
    private JobUrlPolicy() {
    }

    public static Optional<URI> canonicalize(String value, URI base, String allowedHost, String... allowedPathPrefixes) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            URI resolved = base.resolve(value.trim()).normalize();
            String host = resolved.getHost();
            if (!"https".equalsIgnoreCase(resolved.getScheme())
                    || host == null
                    || !host.equalsIgnoreCase(allowedHost)
                    || resolved.getUserInfo() != null
                    || (resolved.getPort() != -1 && resolved.getPort() != 443)
                    || resolved.getRawQuery() != null) {
                return Optional.empty();
            }

            String path = resolved.getPath();
            String rawPath = resolved.getRawPath();
            String lowerRawPath = rawPath == null ? "" : rawPath.toLowerCase(Locale.ROOT);
            boolean allowedPath = allowedPathPrefixes.length == 0;
            for (String prefix : allowedPathPrefixes) {
                if (path != null && path.startsWith(prefix) && path.length() > prefix.length()) {
                    allowedPath = true;
                    break;
                }
            }
            if (!allowedPath
                    || path == null
                    || path.contains("\\")
                    || lowerRawPath.contains("%2f")
                    || lowerRawPath.contains("%5c")
                    || lowerRawPath.contains("%2e")) {
                return Optional.empty();
            }

            return Optional.of(new URI(
                    "https",
                    null,
                    allowedHost.toLowerCase(Locale.ROOT),
                    -1,
                    path,
                    null,
                    null
            ));
        } catch (IllegalArgumentException | URISyntaxException exception) {
            return Optional.empty();
        }
    }
}
