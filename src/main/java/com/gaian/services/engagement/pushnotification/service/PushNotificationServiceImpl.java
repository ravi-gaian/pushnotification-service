package com.gaian.services.engagement.pushnotification.service;

import com.gaian.voxa.Constants;
import com.gaian.voxa.domain.entity.pushnotification.PushNotificationRequestStatusLog;
import com.gaian.voxa.exception.VoxaException;
import com.gaian.voxa.model.PushNotificationRequestWithProvider;
import com.gaian.voxa.provider.api.Provider;
import com.gaian.voxa.provider.api.configuration.Configuration;
import com.gaian.voxa.provider.api.constants.DeviceType;
import com.gaian.voxa.provider.api.constants.EngagementType;
import com.gaian.voxa.provider.api.engagement.pushnotification.PushNotificationEngagementHandler;
import com.gaian.voxa.provider.api.engagement.pushnotification.model.PushNotificationRequestWithConfiguration;
import com.gaian.voxa.repo.pushnotification.PushNotificationRequestStatusLogRepository;
import com.gaian.voxa.service.pushnotification.PushNotificationService;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.DirectProcessor;

@Service
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {
@Autowired
    private DirectProcessor<PushNotificationRequestWithProvider>
        notificationRequestDirectProcessor;

    private Map<String, Provider> providersMap;

    private KafkaTemplate kafkaTemplate;

    @Autowired
    private PushNotificationRequestStatusLogRepository statusLogRepository;

    @Override
    public void setNotificationRequestDirectProcessor(
        DirectProcessor<PushNotificationRequestWithProvider> notificationRequestDirectProcessor) {
        this.notificationRequestDirectProcessor = notificationRequestDirectProcessor;
    }

    @Override
    public void setProvidersMap(Map<String, Provider> providersMap) {
        this.providersMap = providersMap;
    }

    @Override
    public void initialize(Provider provider) {

        log.info("In initialize()");

        PushNotificationEngagementHandler notificationHandler = (PushNotificationEngagementHandler) provider
            .getProviderEngagementHandlers().get(EngagementType.PUSH_NOTIFICATION);
        notificationHandler.start();

        notificationHandler.getPushNotificationHandler().setRequestStream(
            notificationRequestDirectProcessor.filter(pushNotificationRequestWithProvider ->
                pushNotificationRequestWithProvider.getProviderId()
                    .equals(provider.getClass().getCanonicalName())
            ).map(this::createPushNotificationRequestWithConfiguration));
    }

    @Override
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /*
        The below method splits bigger request into smaller requests based on the provider
        configuration and adds the request to kafka
     */
    @Override
    public void handleNotificationRequest(PushNotificationRequestWithProvider request,
        Configuration config) {
        log.debug("In handleNotificationRequest() to send notification for request {}", request);
        Configuration configuration = null;
        if (config != null) {
            configuration = config;
        } else {
            config = providersMap.get(request.getProviderId()) != null ? (
                providersMap.get(request.getProviderId()).getConfigManger() != null ? providersMap
                    .get(request.getProviderId()).getConfigManger()
                    .get(request.getPayload().getConfigId()) : null) : null;
        }
        if (configuration == null) {
            throw new VoxaException(HttpStatus.BAD_REQUEST, "Invalid Config ID");
        }

        PushNotificationRequestStatusLog pushNotificationRequest = new PushNotificationRequestStatusLog();
        pushNotificationRequest.setPayload(request.getPayload());
        pushNotificationRequest.setProviderId(request.getProviderId());
        pushNotificationRequest.setTenantId(request.getTenantId());
        pushNotificationRequest = statusLogRepository.save(pushNotificationRequest);
        log.debug("Push notification request saved into DB, {}", pushNotificationRequest);
        pushNotificationRequest.setConfiguration(configuration);
        request.setRequestId(pushNotificationRequest.getId());
        request.setConfiguration(pushNotificationRequest.getConfiguration());

        String maxPNPerRequest = configuration.getConfig()
            .get(Constants.MAX_PUSH_NOTIFICATION_PER_REQUEST);
        log.info("Push Notification configuration {}, maxPNPerRequest {}", configuration,
            maxPNPerRequest);

        Map<DeviceType, List<String>> to = request.getPayload().getDeviceIdsByType();

        int requestPayloadCount = 0;
        for (DeviceType deviceType : to.keySet()) {
            requestPayloadCount += to.get(deviceType).size();
        }
        log.debug("Number of mobile numbers in the request {}", requestPayloadCount);

        if (maxPNPerRequest != null && Integer.parseInt(maxPNPerRequest) < requestPayloadCount) {
            log.debug(
                "Request from user is bigger than provider configuration, Splitting the sms request");

            for (DeviceType deviceType : to.keySet()) {
                log.debug("Iterating device type {}", deviceType);

                int maxPNPerRequestCount = Integer.parseInt(maxPNPerRequest);
                if (to.get(deviceType).size() > maxPNPerRequestCount) {
                    log.debug(
                        "List of numbers for device type {} is bigger than provider configuration {}",
                        deviceType, maxPNPerRequestCount);

                    Iterable<List<String>> partition = Iterables
                        .partition(to.get(deviceType), maxPNPerRequestCount);

                    for (List<String> list : partition) {
                        Map<DeviceType, List<String>> temp = new HashMap<>();
                        temp.put(deviceType, list);
                        request.getPayload().setDeviceIdsByType(temp);
                        log.debug("Sending the request {} to kafka", request);
                        kafkaTemplate
                            .send(request.getProviderId() + "_" + EngagementType.PUSH_NOTIFICATION,
                                request);
                    }
                } else {
                    log.debug(
                        "List of numbers for device type {} is less than provider configuration",
                        deviceType);
                    Map<DeviceType, List<String>> temp = new HashMap<>();
                    temp.put(deviceType, to.get(deviceType));
                    request.getPayload().setDeviceIdsByType(temp);
                    log.debug("Sending the request {} to kafka", request);
                    kafkaTemplate
                        .send(request.getProviderId() + "_" + EngagementType.SMS,
                            request);
                }
            }
        } else {
            log.debug("*** Request is not added to kafka ***");
            log.debug(
                "Number of mobiles in the request is smaller than the provider configuration");
            sendNotificationRequest(request);
        }
    }

    /*
        This method is called from kafka consumer and
        handles smaller requests. Smaller requests are the
        requests that satisfy provider configuration.
     */
    @Override
    public void sendNotificationRequest(PushNotificationRequestWithProvider request) {
        log.debug("Calling onNext() on flux for request {}", request);
        notificationRequestDirectProcessor.onNext(request);
    }

    @Override
    public PushNotificationRequestWithConfiguration createPushNotificationRequestWithConfiguration(
        PushNotificationRequestWithProvider notificationRequestWithProvider) {
        return new PushNotificationRequestWithConfiguration(
            notificationRequestWithProvider.getTenantId(),
            notificationRequestWithProvider.getRequestId(),
            notificationRequestWithProvider.getProviderId(),
            notificationRequestWithProvider.getPayload(),
            notificationRequestWithProvider.getConfiguration());
    }

    public Optional<PushNotificationRequestStatusLog> getPNRequestStatusLog(String requestId) {
        return statusLogRepository.findById(requestId);
    }


}