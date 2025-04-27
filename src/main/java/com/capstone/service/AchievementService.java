package com.capstone.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.models.Achievement;
import com.capstone.models.User;
import com.capstone.models.UserAchievement;
import com.capstone.models.enums.AchievementType;
import com.capstone.repository.AchievementRepository;
import com.capstone.repository.UserAchievementRepository;
import com.capstone.repository.UserRepository;

@Service
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void checkAndAwardAchievements(String userId, AchievementType type, int currentCount) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found for achievement check: {}", userId);
            return;
        }
        User user = userOpt.get();

        List<Achievement> potentialAchievements = achievementRepository.findByType(type);
        if (potentialAchievements.isEmpty()) {
            logger.debug("No achievement definitions found for type: {}", type);
            return;
        }

        List<UserAchievement> earnedUserAchievements = userAchievementRepository.findByUserIdAndAchievementType(userId, type);
        Set<String> earnedAchievementIds = earnedUserAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        for (Achievement achievement : potentialAchievements) {
            if (currentCount >= achievement.getThreshold() && !earnedAchievementIds.contains(achievement.getId())) {
                UserAchievement newUserAchievement = new UserAchievement(userId, achievement);
                try {
                    userAchievementRepository.save(newUserAchievement);
                    logger.info("User {} (email: {}) awarded achievement: {} (ID: {})", userId, user.getEmail(), achievement.getName(), achievement.getId());

                    if (user.isNotificationsEnabled()) {
                        String message = "Achievement Unlocked: " + achievement.getName() + "! " + achievement.getDescription();
                        // Pass user's email to the notification service
                        notificationService.sendNotificationToUser(user.getEmail(), message);
                        logger.debug("Sent achievement notification to user email {}", user.getEmail());
                    }

                } catch (Exception e) {
                    logger.error("Failed to save UserAchievement for user {} and achievement {}: {}", userId, achievement.getId(), e.getMessage(), e);
                }
            }
        }
    }

    public List<UserAchievement> getEarnedAchievements(String userId) {
        return userAchievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }
}
