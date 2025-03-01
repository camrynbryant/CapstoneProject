package com.capstone.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.capstone.models.StudyGroup;

public interface StudyGroupRepository extends MongoRepository<StudyGroup, String> {
    // Find all study groups that include a specific member
    List<StudyGroup> findByMemberIdsContaining(String memberId);
    
    // Search study groups by a keyword in the group name (case-insensitive)
    List<StudyGroup> findByNameContainingIgnoreCase(String keyword);
    
    // Search study groups by a keyword in the description (case-insensitive)
    List<StudyGroup> findByDescriptionContainingIgnoreCase(String keyword);
    
    // Check if a study group exists with a specific ID that already includes a given member
    boolean existsByIdAndMemberIdsContaining(String groupId, String memberId);
}
