package com.capstone.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.capstone.models.Achievement;
import com.capstone.models.enums.AchievementType;

@Repository
public interface AchievementRepository extends MongoRepository<Achievement, String> {
    List<Achievement> findByType(AchievementType type);
    List<Achievement> findByTypeAndThresholdGreaterThan(AchievementType type, int threshold);
    boolean existsByTypeAndThreshold(AchievementType type, int threshold);
}