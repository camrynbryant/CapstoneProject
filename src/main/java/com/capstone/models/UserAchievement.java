package com.capstone.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.capstone.models.enums.AchievementType;

@Document(collection = "user_achievements")
@CompoundIndex(name = "user_achievement_idx", def = "{'userId' : 1, 'achievementId' : 1}", unique = true)
public class UserAchievement {

    @Id
    private String id;

    @Field("userId") 
    private String userId;

    @Field("achievementId") 
    private String achievementId;

    @Field("earnedAt")
    private LocalDateTime earnedAt;

    private String achievementName;
    private String achievementDescription;
    private String achievementIconUrl;
    private AchievementType achievementType;
    private int achievementThreshold;

    public UserAchievement() {
    }

    public UserAchievement(String userId, Achievement achievement) {
        this.userId = userId;
        this.achievementId = achievement.getId();
        this.earnedAt = LocalDateTime.now();
        this.achievementName = achievement.getName();
        this.achievementDescription = achievement.getDescription();
        this.achievementIconUrl = achievement.getIconUrl();
        this.achievementType = achievement.getType();
        this.achievementThreshold = achievement.getThreshold();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(String achievementId) {
        this.achievementId = achievementId;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }

    public String getAchievementName() {
        return achievementName;
    }

    public void setAchievementName(String achievementName) {
        this.achievementName = achievementName;
    }

    public String getAchievementDescription() {
        return achievementDescription;
    }

    public void setAchievementDescription(String achievementDescription) {
        this.achievementDescription = achievementDescription;
    }

    public String getAchievementIconUrl() {
        return achievementIconUrl;
    }

    public void setAchievementIconUrl(String achievementIconUrl) {
        this.achievementIconUrl = achievementIconUrl;
    }

    public AchievementType getAchievementType() {
        return achievementType;
    }

    public void setAchievementType(AchievementType achievementType) {
        this.achievementType = achievementType;
    }

    public int getAchievementThreshold() {
        return achievementThreshold;
    }

    public void setAchievementThreshold(int achievementThreshold) {
        this.achievementThreshold = achievementThreshold;
    }
}
