package com.capstone.controller;

import com.capstone.models.Notification;
import com.capstone.repository.NotificationRepository;
import com.capstone.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return notificationService.getNotificationsForUser(userId);
    }
        @PutMapping("/{notificationId}/read")
        public Notification markAsRead(@PathVariable String notificationId) {
            return notificationService.markAsRead(notificationId);
        }

        @GetMapping("/{userId}/unreadCount")
        public long getUnreadCount(@PathVariable String userId) {
            return notificationService.getUnreadCount(userId);
        }

        @GetMapping("/{userId}/upcoming")
        public ResponseEntity<?> getUpcomingNotifications(@PathVariable String userId) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);
            List<Notification> notifications = notificationRepository
                .findByUserIdAndCreatedAtAfterAndReadFalse(userId, thirtyMinutesAgo);
            return ResponseEntity.ok(notifications);
    }
}
