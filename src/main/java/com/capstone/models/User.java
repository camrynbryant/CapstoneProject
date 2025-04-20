package com.capstone.models;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private boolean notificationsEnabled;
    private String profilePictureUrl;
    
    @Field("studyInterests")
    private Set<String> studyInterests = new HashSet<>();

        // --- Achievement Counters ---
    private int studySessionsCreatedCount = 0;
    private int studyGroupsCreatedCount = 0;
    private int studySessionsJoinedCount = 0;
    private int filesUploadedCount = 0;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.notificationsEnabled = true;
        this.studyInterests = new HashSet<>();
        this.studySessionsCreatedCount = 0;
        this.studyGroupsCreatedCount = 0;
        this.studySessionsJoinedCount = 0;
        this.filesUploadedCount = 0;
    }


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
  
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
  
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
  
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
  
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public Set<String> getStudyInterests() {
        return studyInterests;
    }

    public void setStudyInterests(Set<String> studyInterests) {
        this.studyInterests = studyInterests;
    }
    

    // --- Achievement Counter Getters/Setters ---
    public int getStudySessionsCreatedCount() { 
        return studySessionsCreatedCount; 
    }
    public void setStudySessionsCreatedCount(int studySessionsCreatedCount) { 
        this.studySessionsCreatedCount = studySessionsCreatedCount; 
    }
    public int getStudyGroupsCreatedCount() { 
        return studyGroupsCreatedCount; 
    }
    public void setStudyGroupsCreatedCount(int studyGroupsCreatedCount) { 
        this.studyGroupsCreatedCount = studyGroupsCreatedCount; 
    }
    public int getStudySessionsJoinedCount() { 
        return studySessionsJoinedCount; 
    }
    public void setStudySessionsJoinedCount(int studySessionsJoinedCount) { 
        this.studySessionsJoinedCount = studySessionsJoinedCount; 
    }
    public int getFilesUploadedCount() { 
        return filesUploadedCount; 
    }
    public void setFilesUploadedCount(int filesUploadedCount) { 
        this.filesUploadedCount = filesUploadedCount; 
    }
    
    // --- Convenience methods for incrementing counters ---
    public int incrementStudySessionsCreated() {
        this.studySessionsCreatedCount++;
        return this.studySessionsCreatedCount;
    }
    public int incrementStudyGroupsCreated() {
        this.studyGroupsCreatedCount++;
        return this.studyGroupsCreatedCount;
    }
    public int incrementStudySessionsJoined() {
        this.studySessionsJoinedCount++;
        return this.studySessionsJoinedCount;
    }
     public int incrementFilesUploaded() {
        this.filesUploadedCount++;
        return this.filesUploadedCount;
    }
}
