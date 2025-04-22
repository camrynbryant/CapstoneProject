package com.capstone.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.models.User;
import com.capstone.models.UserAchievement;
import com.capstone.repository.UserRepository;
import com.capstone.service.AchievementService;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private static final Logger logger = LoggerFactory.getLogger(AchievementController.class);

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<List<UserAchievement>> getMyAchievements(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = userDetails.getUsername();

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            logger.warn("Authenticated user {} not found in database for fetching achievements.", userEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String userId = userOpt.get().getId();

        try {
            List<UserAchievement> achievements = achievementService.getEarnedAchievements(userId);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            logger.error("Error fetching achievements for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<UserAchievement>> getUserAchievements(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("User {} not found in database for fetching achievements.", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String userId = userOpt.get().getId();
        
        try {
            List<UserAchievement> achievements = achievementService.getEarnedAchievements(userId);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            logger.error("Error fetching achievements for user {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
