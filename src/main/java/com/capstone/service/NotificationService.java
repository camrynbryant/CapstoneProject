package com.capstone.service;

import com.capstone.models.Notification;
import com.capstone.models.User;
import com.capstone.repository.NotificationRepository;
import com.capstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

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
