package com.gaian.services.engagement.pushnotification.model;

import com.gaian.voxa.provider.api.configuration.Configuration;
import com.gaian.voxa.provider.api.engagement.pushnotification.model.PushNotificationRequest;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushNotificationRequestWithProvider {

    @NotNull
    private PushNotificationRequest payload;
    @NotNull
    private String providerId;
    @NotNull
    private String tenantId;
    private String requestId;
    private Configuration configuration;

    public PushNotificationRequestWithProvider(@NotNull PushNotificationRequest payload, @NotNull String providerId,
        @NotNull String tenantId) {
        this.payload = payload;
        this.providerId = providerId;
        this.tenantId = tenantId;
    }
}
