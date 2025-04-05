package com.capstone.controller;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class NotificationSettingsController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/{userId}/notifications")
    public User updateNotificationSetting(@PathVariable String userId, @RequestBody NotificationSettingRequest request) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setNotificationsEnabled(request.isNotificationsEnabled());
        return userRepository.save(user);
    }
    
    @GetMapping("/{userId}")
    public User getUserSettings(@PathVariable String userId) {
        return userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    
    public static class NotificationSettingRequest {
        private boolean notificationsEnabled;

        public boolean isNotificationsEnabled() {
            return notificationsEnabled;
        }

        public void setNotificationsEnabled(boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }
    }
}

