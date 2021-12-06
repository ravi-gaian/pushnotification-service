package com.gaian.services.engagement.pushnotification.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.gaian.services.error.CommonErrors;
import com.gaian.services.error.Error;

/**
 * All public final static Error codes and the messages
 */
public class Errors extends CommonErrors {

    public static final Error INVALID_REQUEST = new Error(
        BAD_REQUEST,
        4001,
        "The  request is invalid ",
        "Kindly verify if your request is valid or contact the support team"
    );

    public static final Error PUSHNOTIFICATION_UPLOAD_FAILRE = new Error(
        INTERNAL_SERVER_ERROR,
        5001,
        "Failed to post content on twitter page ",
        "Kindly verify if your request is valid or contact the support team"
    );
}
