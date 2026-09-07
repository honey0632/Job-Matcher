// Signals a failure while extracting text from a resume.

package com.honey.jobfetcher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ResumeExtractionException extends RuntimeException {

    public ResumeExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
