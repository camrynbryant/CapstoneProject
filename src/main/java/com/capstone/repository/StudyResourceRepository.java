package com.capstone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.capstone.models.StudyResource;

@Repository
public interface StudyResourceRepository extends MongoRepository<StudyResource, String> {
    List<StudyResource> findByGroupIdOrderByUploadDateDesc(String groupId);
    Optional<StudyResource> findByIdAndGroupId(String id, String groupId); 
}