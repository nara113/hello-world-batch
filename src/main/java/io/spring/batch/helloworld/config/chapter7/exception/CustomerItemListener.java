package io.spring.batch.helloworld.config.chapter7.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.item.file.FlatFileParseException;

@Slf4j
public class CustomerItemListener {

    @OnReadError
    public void onReadError(Exception e) {
        if (e instanceof FlatFileParseException) {
            FlatFileParseException fe = (FlatFileParseException) e;

            String errorMessage = "An error occurred while processing the "
                    + fe.getLineNumber()
                    + " line of the file. Below was the faulty input.\n"
                    + fe.getInput();

            log.error(errorMessage, fe);
        } else {
            log.error("An error has occurred", e);
        }
    }
}
