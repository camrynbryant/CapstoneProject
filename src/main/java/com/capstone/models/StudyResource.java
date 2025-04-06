package com.capstone.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "study_resources")
public class StudyResource {

    @Id
    private String id;
    private String filename;
    private String contentType;
    private long size;
    private LocalDateTime uploadDate;
    private String uploaderEmail; 
    private String groupId; 
    private String gridFsId; 

    public StudyResource() {
        this.uploadDate = LocalDateTime.now();
    }

    public StudyResource(String filename, String contentType, long size, String uploaderEmail, String groupId, String gridFsId) {
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.uploaderEmail = uploaderEmail;
        this.groupId = groupId;
        this.gridFsId = gridFsId;
        this.uploadDate = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGridFsId() {
        return gridFsId;
    }

    public void setGridFsId(String gridFsId) {
        this.gridFsId = gridFsId;
    }
}