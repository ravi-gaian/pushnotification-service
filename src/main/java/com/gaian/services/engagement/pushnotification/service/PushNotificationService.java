package com.gaian.services.engagement.pushnotification.service;

import com.gaian.voxa.model.PushNotificationRequestWithProvider;
import com.gaian.voxa.provider.api.Provider;
import com.gaian.voxa.provider.api.engagement.pushnotification.model.PushNotificationRequestWithConfiguration;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.DirectProcessor;

@Component
public interface PushNotificationService {

    void setNotificationRequestDirectProcessor(
        final DirectProcessor<PushNotificationRequestWithProvider> notificationRequestDirectProcessor);

    void setProvidersMap(final Map<String, Provider> providersMap);

    void initialize(Provider provider);

    void setKafkaTemplate(KafkaTemplate kafkaTemplate);

    void handleNotificationRequest(PushNotificationRequestWithProvider request);

    void sendNotificationRequest(PushNotificationRequestWithProvider request);

    PushNotificationRequestWithConfiguration createPushNotificationRequestWithConfiguration(
        PushNotificationRequestWithProvider notificationRequestWithProvider);
}