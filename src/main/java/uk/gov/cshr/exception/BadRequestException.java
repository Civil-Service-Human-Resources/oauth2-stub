package uk.gov.cshr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Bad Request")
public class BadRequestException extends RuntimeException {

    public BadRequestException(Throwable cause) {
        super(cause);
    }
}
