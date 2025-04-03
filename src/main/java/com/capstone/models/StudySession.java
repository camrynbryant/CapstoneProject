package com.capstone.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "study_sessions")
public class StudySession {
    @Id
    private String id;
    
    @Field("group_id")
    private String groupId;
    
    @Field("topic")
    private String topic;
    
    @Field("description")
    private String description;
    
    @Field("start_time")
    private LocalDateTime startTime;
    
    @Field("end_time")
    private LocalDateTime endTime;
    
    @Field("location")
    private String location;
    
    @Field("participant_ids")
    private List<String> participantIds;
    
    @Field("created_by")
    private String createdBy;
    
    public StudySession() {}

    public StudySession(String id, String groupId, String topic, String description,
                        LocalDateTime startTime, LocalDateTime endTime,
                        String location, List<String> participantIds, String createdBy) {
        this.id = id;
        this.groupId = groupId;
        this.topic = topic;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.participantIds = participantIds;
        this.createdBy = createdBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "StudySession{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", topic='" + topic + '\'' +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", location='" + location + '\'' +
                ", participantIds=" + participantIds +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}