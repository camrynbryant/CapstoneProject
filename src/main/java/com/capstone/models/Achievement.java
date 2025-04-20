package com.capstone.models;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.capstone.models.enums.AchievementType;

@Document(collection = "achievements")
@CompoundIndex(name = "type_threshold_idx", def = "{'type' : 1, 'threshold' : 1}", unique = true)
public class Achievement {

    @Id
    private String id;
    private String name; 
    private String description; 
    private AchievementType type; 
    private int threshold; 
    private String iconUrl; 

    public Achievement() {}

    public Achievement(String name, String description, AchievementType type, int threshold, String iconUrl) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.threshold = threshold;
        this.iconUrl = iconUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AchievementType getType() {
        return type;
    }

    public void setType(AchievementType type) {
        this.type = type;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}