package com.gaian.services.engagement.pushnotification.config;

import com.gaian.services.engagement.error.exception.ServiceProviderSetupException;
import com.gaian.services.engagement.model.request.channel.ChannelType;
import com.gaian.voxa.model.PushNotificationRequestWithProvider;
import com.gaian.voxa.provider.api.Provider;
import com.gaian.voxa.provider.api.constants.EngagementType;
import com.gaian.voxa.service.config.EngagementConfigurationService;
import com.gaian.voxa.service.pushnotification.PushNotificationService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.DirectProcessor;

@Slf4j
@Configuration
public class PushNotificationConfig {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private EngagementConfigurationService configurationService;

    @Bean
    public DirectProcessor<PushNotificationRequestWithProvider> notificationRequestDirectProcessor() {
        return DirectProcessor.create();
    }

    @Bean
    public Map<String, Provider> serviceProviders() {

        Map<String, Provider> configuredServiceProviders = new HashMap<>();

        ServiceLoader<Provider> serviceProviders = ServiceLoader.load(Provider.class);
        serviceProviders.iterator().forEachRemaining(serviceProvider -> {

            try {
                Optional.ofNullable(serviceProvider).map(Provider::getProviderEngagementHandlers)
                    .map(Map::keySet).map(Set::parallelStream).ifPresent(stream ->
                    stream.filter(EngagementType.PUSH_NOTIFICATION::equals)
                        .findAny().map(channel ->
                        configureServiceProvider(serviceProvider)).ifPresent(providerName ->
                        configuredServiceProviders.put(providerName, serviceProvider)));

            } catch (Exception e) {
                String errorMessage = String.format(
                    "Error while loading a service provider '%s' for EMAIL ", serviceProvider);
                throw new ServiceProviderSetupException(errorMessage, e);
            }
        });

        return configuredServiceProviders;
    }

    public String configureServiceProvider(Provider serviceProvider) {

        String providerName = serviceProvider.getClass().getCanonicalName();
        log.info("Setting up provider {} ", providerName);

        pushNotificationService.initialize(serviceProvider);
        serviceProvider.loadConfiguration(
            configurationService.getConfigurationMap(providerName));

        return providerName;
    }


}
