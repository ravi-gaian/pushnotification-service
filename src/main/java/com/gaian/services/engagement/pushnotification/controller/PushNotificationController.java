package com.gaian.services.engagement.pushnotification.controller;

import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.request.channel.pushnotifications.PushNotificationChannel;
import com.gaian.services.engagement.pushnotification.validator.Validators;
import com.gaian.voxa.model.PushNotificationRequestWithProvider;
import com.gaian.voxa.provider.api.configuration.Configuration;
import com.gaian.voxa.provider.api.engagement.pushnotification.model.PushNotificationRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Api controller to deliver SMS engagements
 */
@Slf4j
@RestController
@RequestMapping("/v1.0/{tenantID}/pushnotification")
public class PushNotificationController {

    @Autowired
    private Validators validators;

    @Autowired
    private com.gaian.services.engagement.pushnotification.service.PushNotificationServiceImpl pushNotificationProcessor;

    /**
     * share content to twitter page.
     *
     * @param tenantId     ID of tenant
     * @param inputRequest twitter engagement request
     * @return Trigger Response
     */
    @PostMapping
    public ResponseEntity<String> upload(@PathVariable("tenantID") String tenantId,
        @Valid @RequestBody DeliveryRequest requestModel) {
        log.info(">>> received request" + requestModel.toString());
        PushNotificationChannel pushChannel = (PushNotificationChannel) requestModel.getEngagement()
            .getChannel();
        Configuration configuration = validators.validateConfiguration(requestModel);
        pushNotificationProcessor.handleNotificationRequest(
            new PushNotificationRequestWithProvider(pushChannel.getPayload(),
                pushChannel.getProviderId(), tenantId), configuration);

        return ResponseEntity.ok().build();
    }
}
