package com.gaian.services.engagement.pushnotification.kafka;

import static com.gaian.services.engagement.error.ErrorTemplates.ERROR_NULL_KAFKA_MESSAGE;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.joda.time.DateTime.now;

import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.pushnotification.service.PushNotificationServiceImpl;
import com.gaian.services.engagement.pushnotification.validator.Validators;
import com.gaian.services.exception.KafkaConsumptionException;
import com.gaian.voxa.provider.api.configuration.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task subscriber implementation for twitter engagements
 */
@Slf4j
@Component
public class PushNotificationTaskSubscriber {

    @Autowired
    private Validators validators;

    @Autowired
    private PushNotificationServiceImpl pushNotificationProcessor;

    /**
     * Processes twitter engagements
     *
     * @param message Event consumed from kafka topic
     */
    public void performTask(ConsumerRecord<String, DeliveryRequest> message) {

        DeliveryRequest task = message.value();
        DateTime startTime = now();

        log.info("*** Received new engagement task from {} from partition {} & offset {} : {}",
            message.topic(), message.partition(), message.offset(), task);

        try {
            if (isNull(task))
                throw new KafkaConsumptionException(format(ERROR_NULL_KAFKA_MESSAGE, message.topic()));

            Configuration configuration = validators.validateConfiguration(task);
//            pushNotificationProcessor.handleNotificationRequest(task, configuration);

            log.info("Completed engagement task in {} millis from {} from partition {} & offset {} : {}",
                new Period(startTime, now()).getMillis(), message.topic(), message.partition(), message.offset(), task);

        } catch (Exception exception) {
            log.error("Error while executing an engagement task in {} millis : {} ",
                new Period(startTime, now()).getMillis(), task, exception);
        }
    }
}
