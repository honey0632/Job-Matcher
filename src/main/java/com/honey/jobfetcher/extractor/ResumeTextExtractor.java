package com.honey.jobfetcher.extractor;

import java.io.IOException;
import java.nio.file.Path;

public interface ResumeTextExtractor {

    String extract(Path file, String contentType) throws IOException;
}
