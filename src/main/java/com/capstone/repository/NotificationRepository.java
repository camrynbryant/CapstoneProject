package com.capstone.repository;

import com.capstone.models.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

        long countByUserIdAndReadFalse(String userId);

            List<Notification> findByUserIdAndCreatedAtAfterAndReadFalse(String userId, LocalDateTime time);

}

