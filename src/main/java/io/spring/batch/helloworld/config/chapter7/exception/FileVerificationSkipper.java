package io.spring.batch.helloworld.config.chapter7.exception;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

import java.io.FileNotFoundException;
import java.text.ParseException;

public class FileVerificationSkipper implements SkipPolicy {
    @Override
    public boolean shouldSkip(Throwable throwable, int i) throws SkipLimitExceededException {
        if (throwable instanceof FileNotFoundException) {
            return false;
        } else if (throwable instanceof ParseException && i <= 10) {
            return true;
        } else {
            return false;
        }
    }
}
