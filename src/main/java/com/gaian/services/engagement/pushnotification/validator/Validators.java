package com.gaian.services.engagement.pushnotification.validator;

import static com.gaian.voxa.provider.api.constants.EngagementType.PUSH_NOTIFICATION;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.request.channel.ChannelType;
import com.gaian.services.engagement.model.request.channel.pushnotifications.PushNotificationChannel;
import com.gaian.services.engagement.model.request.channel.twitter.Attachment;
import com.gaian.services.engagement.model.request.channel.twitter.TwitterChannel;
import com.gaian.services.engagement.model.request.channel.twitter.TwitterConfig;
import com.gaian.services.engagement.pushnotification.error.exception.InvalidRequestException;
import com.gaian.services.engagement.pushnotification.error.exception.PushNotificationDeliveryException;
import com.gaian.voxa.provider.api.configuration.Configuration;
import com.gaian.voxa.service.config.EngagementConfigurationService;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Validator implementation for twitter related operations
 */
@Slf4j
@Component
public class Validators {

    @Autowired
    private EngagementConfigurationService configurationService;

    @Value("${push.provider.default:com.gaian.voxa.fcm.ProviderImpl}")
    private String defaultProvider;

    /**
     * Validates twitter engagement request
     *
     * @param inputPayload twitter engagement request
     */
    private void validateRequest(DeliveryRequest inputPayload) {

        if (isNull(inputPayload)) {
            throw new InvalidRequestException();
        }

        EngagementDBModel twitterEngagement = inputPayload.getEngagement();

        if (isNull(twitterEngagement)) {
            throw new InvalidRequestException("Invalid engagement request! ");

        } else {

            ofNullable(twitterEngagement.getChannel()).map(channel -> {

                if (!(ChannelType.PUSH_NOTIFICATION.equals(channel.getChannelType()))
                    || !(channel instanceof PushNotificationChannel)) {
                    throw new InvalidRequestException(
                        "Not an engagement of push notification channel! ");
                }

                channel.validate();
                return channel;

            }).orElseThrow(
                () -> new InvalidRequestException("Engagement must have a channel! "));
        }
    }


    /**
     * Validates twitter engagement request
     *
     * @param inputPayload twitter engagement request
     */
    private void validatePushNotificationConfigurationRequest(DeliveryRequest inputPayload) {
        TwitterChannel channel = (TwitterChannel) inputPayload.getEngagement().getChannel();
        TwitterConfig config = channel.getConfig();
        if (!StringUtils.hasText(config.getTitle())) {
            throw new InvalidRequestException("missing title in twitter post request");
        }
        if(!CollectionUtils.isEmpty(config.getAttachments())){
            for (Attachment attachment : config.getAttachments()) {
                if (!StringUtils.hasText(attachment.getUrl())){
                   throw new InvalidRequestException("attachment must contain URL");
                }
                try {
                    new URL(attachment.getUrl()).toURI();
                } catch (URISyntaxException | MalformedURLException e) {
                    log.error("attachment url is not valid", e);
                    throw new InvalidRequestException("attachment URL is not valid. Please verify URI once");
                }
            }
        }
    }

    /**
     * Validates configuration to be used to share content on twitter page
     *
     * @param deliveryRequest twitter request
     * @return Configuration with required tokens
     */
    public Configuration validateConfiguration(DeliveryRequest deliveryRequest) {

        validateRequest(deliveryRequest);
        PushNotificationChannel pushNotificationsChannel = (PushNotificationChannel) deliveryRequest
            .getEngagement()
            .getChannel();
        String provider = ofNullable(pushNotificationsChannel.getProviderId()).orElse(defaultProvider);
        pushNotificationsChannel.setProviderId(provider);
        Map<String, String> providedConfig = pushNotificationsChannel.getProviders();
        Map<String, String> pNConfig;
        if (providedConfig.size() > 0) {
            pNConfig = providedConfig;
        } else {
            Configuration defaultConfig = ofNullable(
                configurationService.getConfigurationMap(provider))
                .map(configs -> ofNullable(pushNotificationsChannel.getConfigId())
                    .map(configs::get)
                    .orElse(configs.values().iterator().next()))
                .filter(config -> PUSH_NOTIFICATION.equals(config.getEngagementType()))
                .orElseThrow(() -> new IllegalArgumentException("No valid defaultConfig found! "));
            defaultConfig.setProviderId(provider);
            pNConfig = defaultConfig.getConfig();
        }

        if (CollectionUtils.isEmpty(pNConfig)) {
            log.error("push notification config is missing");
            throw new PushNotificationDeliveryException("No proper configuration found");
        }

//        validatePushNotificationConfigurationRequest(deliveryRequest);
        return new Configuration(PUSH_NOTIFICATION, String.valueOf(new UUID(5, 1)), deliveryRequest.getEngagement()
            .getTenantId(), pNConfig);
    }
}
