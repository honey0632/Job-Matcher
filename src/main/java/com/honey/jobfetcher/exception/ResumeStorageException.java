// Signals a failure while storing or reading a resume.

package com.honey.jobfetcher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ResumeStorageException extends RuntimeException {

    public ResumeStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
