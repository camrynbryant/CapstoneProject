package com.capstone.config;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.capstone.models.Achievement;
import com.capstone.models.enums.AchievementType;
import com.capstone.repository.AchievementRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AchievementRepository achievementRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking and initializing achievement definitions...");
        initializeAchievements();
        logger.info("Achievement definition initialization complete.");
    }

    private void initializeAchievements() {
        List<Integer> thresholds = Arrays.asList(1, 5, 10, 20, 50, 100); 

        // Study Session Created
        createAchievementsForType(AchievementType.STUDY_SESSION_CREATED, "Session Starter", "Created %d study session(s)", thresholds);

        // Study Group Created
        createAchievementsForType(AchievementType.STUDY_GROUP_CREATED, "Group Founder", "Created %d study group(s)", thresholds);

        // Study Session Joined
        createAchievementsForType(AchievementType.STUDY_SESSION_JOINED, "Active Participant", "Joined %d study session(s)", thresholds);

        // File Uploaded
        createAchievementsForType(AchievementType.FILE_UPLOADED, "Resource Contributor", "Uploaded %d file(s)", thresholds);
    }

    private void createAchievementsForType(AchievementType type, String baseName, String descriptionFormat, List<Integer> thresholds) {
        for (int threshold : thresholds) {
            String name = baseName + (threshold == 1 ? "" : " " + getRomanNumeral(threshold)); 
            String description = String.format(descriptionFormat, threshold);

            boolean alreadyExists = achievementRepository.existsByTypeAndThreshold(type, threshold);


            if (!alreadyExists) {
                // Simple icon placeholder - replace with actual URLs/paths if available
                String iconUrl = "/icons/achievement_" + type.name().toLowerCase() + "_" + threshold + ".png";
                Achievement achievement = new Achievement(name, description, type, threshold, iconUrl);
                try {
                    achievementRepository.save(achievement);
                    logger.info("Created achievement definition: {}", name);
                } catch (Exception e) {
                     logger.warn("Could not create achievement definition '{}' (might already exist): {}", name, e.getMessage());
                }
            }
        }
    }

    private String getRomanNumeral(int number) {
        if (number >= 100) return "C";
        if (number >= 90) return "XC";
        if (number >= 50) return "L" + getRomanNumeral(number - 50);
        if (number >= 40) return "XL";
        if (number >= 10) return "X" + getRomanNumeral(number - 10);
        if (number >= 9) return "IX";
        if (number >= 5) return "V" + getRomanNumeral(number - 5);
        if (number >= 4) return "IV";
        if (number >= 1) return "I" + getRomanNumeral(number - 1);
        return "";
    }
}