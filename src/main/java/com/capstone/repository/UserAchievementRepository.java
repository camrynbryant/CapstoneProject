package com.capstone.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.capstone.models.UserAchievement;
import com.capstone.models.enums.AchievementType;

@Repository
public interface UserAchievementRepository extends MongoRepository<UserAchievement, String> {
    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(String userId);
    boolean existsByUserIdAndAchievementId(String userId, String achievementId);
    List<UserAchievement> findByUserIdAndAchievementType(String userId, AchievementType type);
}