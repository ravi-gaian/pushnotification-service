package com.gaian.services.engagement.pushnotification.error.exception;

import static com.gaian.services.engagement.pushnotification.error.Errors.INVALID_REQUEST;

import com.gaian.services.exception.ValidationException;

/**
 * Exception model to be raised if the SMS request is invalid
 */
public class InvalidRequestException extends ValidationException {

    public InvalidRequestException() {
        super(INVALID_REQUEST);
    }

    public InvalidRequestException(String errorMessage) {
        super(INVALID_REQUEST, errorMessage);
    }
}
