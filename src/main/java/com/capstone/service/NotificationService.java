package com.capstone.service;

import com.capstone.models.Notification;
import com.capstone.models.User;
import com.capstone.repository.NotificationRepository;
import com.capstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            // Use email-based lookup
            Optional<User> userOpt = userRepo.findByEmail(userId);
            if (userOpt.isPresent() && userOpt.get().isNotificationsEnabled()) {
                notificationRepo.save(new Notification(userId, message));
            }
        }
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}