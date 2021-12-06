package com.gaian.services.engagement.pushnotification.model;

import com.gaian.voxa.domain.entity.pushnotification.PushNotificationRequestStatusLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PushNotificationRequestStatusLogRepository extends
    MongoRepository<PushNotificationRequestStatusLog, String> {

}
