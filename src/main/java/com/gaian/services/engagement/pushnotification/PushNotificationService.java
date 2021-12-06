package com.gaian.services.engagement.pushnotification;

import static org.springframework.boot.SpringApplication.run;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

import com.gaian.services.engagement.repo.EngagementInstanceRepo;
import com.gaian.services.engagement.service.EngagementProcessor;
import com.gaian.services.engagement.service.EngagementService;
import com.gaian.voxa.repo.EngagementConfigurationRepository;
import com.gaian.voxa.service.config.EngagementConfigurationService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class for twitter-service. This is where the execution starts.
 */
@EnableMongoRepositories(
    basePackageClasses = {
        EngagementInstanceRepo.class,
        //SmsRequestStatusLogRepository.class,
        EngagementConfigurationRepository.class,
    }
)
@ComponentScan(
    basePackageClasses = {
        EngagementConfigurationService.class,
        EngagementInstanceRepo.class,
        //SmsRequestStatusLogRepository.class,
        EngagementConfigurationRepository.class,
    },
    basePackages = {
        "com.gaian.services.engagement.pushnotification",
    },
    excludeFilters = {
        @Filter(type = ASSIGNABLE_TYPE, value = EngagementProcessor.class),
        @Filter(type = ASSIGNABLE_TYPE, value = EngagementService.class)
    }
)
@EnableAsync
@SpringBootApplication
public class PushNotificationService {

    public static void main(String[] args) {
        run(PushNotificationService.class, args);
    }
}
