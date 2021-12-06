package com.gaian.services.engagement.pushnotification.kafka;

import com.gaian.services.engagement.model.DeliveryRequest;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class DeliveryRequestDeserializer extends JsonDeserializer<DeliveryRequest> {
}
