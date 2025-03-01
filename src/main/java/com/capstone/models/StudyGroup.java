package com.capstone.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "study_groups")
public class StudyGroup {
    @Id
    private String id;
    private String name;
    private String description;
    private String owner; // The creator (president) of the group
    private List<String> memberIds; // store user IDs

    public StudyGroup() {}

    public StudyGroup(String name, String description, String owner, List<String> memberIds) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.memberIds = memberIds;
    }

    // Getters and setters
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

    public String getOwner() { 
        return owner; 
    }
    
    public void setOwner(String owner) { 
        this.owner = owner; 
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }
}
