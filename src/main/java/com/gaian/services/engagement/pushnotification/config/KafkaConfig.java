package com.gaian.services.engagement.pushnotification.config;

import static io.vertx.core.Vertx.vertx;
import static java.lang.String.format;
import static java.time.Duration.ofMillis;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;

import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.pushnotification.kafka.DeliveryRequestDeserializer;
import com.gaian.services.engagement.pushnotification.kafka.PushNotificationTaskSubscriber;
import com.gaian.services.exception.KafkaException;
import dev.snowdrop.vertx.kafka.KafkaConsumerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Data
@Slf4j
@EnableKafka
@Configuration
@ConfigurationProperties(prefix = "kafka")
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
public class KafkaConfig {

    @Autowired
    private PushNotificationTaskSubscriber subscriber;

    @Autowired
    private KafkaConsumerFactory consumerFactory;

    @Value("${kafka.PushnotificationTaskConsumer.topic:engagement.delivery.pushnotification}")
    private String engagementTopic;

    private Map<String, String> pushNotificationTaskConsumer;

    private final StringSerializer stringSerializer = new StringSerializer();

    /**
     * Setting up kafka consumers
     */
    @PostConstruct
    public void setUpEngagementEventListeners() {

        pushNotificationTaskConsumer.put("key.deserializer", StringDeserializer.class.getName());
        pushNotificationTaskConsumer.put("value.deserializer", DeliveryRequestDeserializer.class.getName());

        Integer concurrency = ofNullable(pushNotificationTaskConsumer.get("concurrency")).map(Integer::valueOf).orElse(30);

        range(0, concurrency).forEach(index ->

            consumerFactory.<String, DeliveryRequest>create(pushNotificationTaskConsumer).doOnVertxConsumer(
                consumer -> consumer.subscribe(engagementTopic, subscription -> {

                    if (subscription.succeeded()) {
                        log.info("Consumer {} subscribed to topic {}", index, engagementTopic);

                        Long minPollIntervalMs = ofNullable(pushNotificationTaskConsumer.get("min.poll.interval.ms")).map(Long::valueOf)
                            .orElse(10000L);

                        vertx().setPeriodic(minPollIntervalMs, poller -> pollMessages(consumer, index));

                    } else {
                        throw new KafkaException(
                            format("Error subscribing to topic %s", engagementTopic), subscription.cause());
                    }
                })
            ).block()
        );
    }

    /**
     * Polling messages from topic
     *
     * @param consumer Kafka consumer
     * @param consumerId ID of consumer
     */
    public void pollMessages(
        KafkaConsumer<String, DeliveryRequest> consumer, int consumerId) {

        log.debug("Trying to poll from {}", engagementTopic);

        Long pollTimeoutMs = ofNullable(pushNotificationTaskConsumer.get("poll.timeout.ms"))
            .map(Long::valueOf).orElse(1000L);

        consumer.poll(ofMillis(pollTimeoutMs), response -> {

            if (response.succeeded()) {

                KafkaConsumerRecords<String, DeliveryRequest> events = response.result();
                if (!events.isEmpty()) {

                    ConsumerRecords<String, DeliveryRequest> messages = events.records();

                    log.info("{} messages polled from partitions {} by consumer {}",
                        events.size(), messages.partitions(), consumerId);

                    consumer.pause();
                    log.debug("Consumer paused");

                    messages.forEach(task -> {

                        subscriber.performTask(task);
                        commit(consumer, consumerId, messages);
                    });

                    consumer.resume();
                    log.debug("Consumer resumed");
                }
                pollMessages(consumer, consumerId);

            } else {
                log.error("Failed to poll messages from {} ", engagementTopic, response.cause());
            }
        });
    }

    /**
     * Commit the messages that have been already processed
     *
     * @param consumer Kafka consumer
     * @param consumerId ID of consumer
     * @param messages Messages to be committed
     */
    private void commit(
        KafkaConsumer<String, DeliveryRequest> consumer, int consumerId,
        ConsumerRecords<String, DeliveryRequest> messages) {

        Boolean autoCommitEnabled = ofNullable(pushNotificationTaskConsumer.get("enable.auto.commit"))
            .map(Boolean::valueOf).orElse(false);

        if (!autoCommitEnabled) {
            consumer.commit(commitResponse -> {
                if (commitResponse.failed()) {
                    log.error("Consumer {} failed to commit on partition {} of {}",
                        consumerId, messages.partitions(),
                        engagementTopic, commitResponse.cause());
                } else {
                    log.info("Consumer {} committed message on partition {}",
                        consumerId, messages.partitions());
                }
            });
        }
    }
}
