package com.capstone.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.models.Notification;
import com.capstone.repository.NotificationRepository;
import com.capstone.repository.UserRepository;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepo;
    
    @Autowired
    private UserRepository userRepo;

    public void sendNotificationToUsers(List<String> userIds, String message) {
        for (String userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setMessage(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notificationRepo.save(notification);
        }
    }

    public void sendNotificationToUser(String userId, String message) {
        if (userId == null || userId.isEmpty()) {
            logger.warn("Attempted to send notification to null or empty userId.");
            return;
        }
        try {
            Notification notification = new Notification();

            notification.setUserId(userId);
            notification.setMessage(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notificationRepo.save(notification);
            logger.debug("Saved notification via sendNotificationToUser for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to save notification for userId {}: {}", userId, e.getMessage(), e);
        }
    }
    

    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return notificationRepo.save(notification);
    }
    public long getUnreadCount(String userId) {
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }

}
