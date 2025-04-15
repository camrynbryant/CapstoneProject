package com.capstone.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.capstone.models.StudyGroup;

public interface StudyGroupRepository extends MongoRepository<StudyGroup, String> {
    List<StudyGroup> findByMemberIdsContaining(String memberId);
    
    List<StudyGroup> findByNameContainingIgnoreCase(String keyword);
    
    List<StudyGroup> findByDescriptionContainingIgnoreCase(String keyword);
    
    boolean existsByIdAndMemberIdsContaining(String groupId, String memberEmail);
}
