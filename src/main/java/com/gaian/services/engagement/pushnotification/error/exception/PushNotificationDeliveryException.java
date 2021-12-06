package com.gaian.services.engagement.pushnotification.error.exception;

import static com.gaian.services.engagement.pushnotification.error.Errors.PUSHNOTIFICATION_UPLOAD_FAILRE;

import com.gaian.services.exception.ValidationException;

/**
 * Exception to be raised if the SMS delivery fails
 */
public class PushNotificationDeliveryException extends ValidationException {

    public PushNotificationDeliveryException() {
        super(PUSHNOTIFICATION_UPLOAD_FAILRE);
    }

    public PushNotificationDeliveryException(String errorMessage) {
        super(PUSHNOTIFICATION_UPLOAD_FAILRE, errorMessage);
    }

    public PushNotificationDeliveryException(Throwable throwable) {
        super(PUSHNOTIFICATION_UPLOAD_FAILRE, throwable);
    }

    public PushNotificationDeliveryException(String errorMessage, Throwable throwable) {
        super(PUSHNOTIFICATION_UPLOAD_FAILRE, errorMessage, throwable);
    }
}
