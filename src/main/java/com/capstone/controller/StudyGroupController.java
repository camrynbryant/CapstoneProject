package com.capstone.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.models.StudyGroup;
import com.capstone.models.User;
import com.capstone.models.enums.AchievementType;
import com.capstone.repository.StudyGroupRepository;
import com.capstone.repository.UserRepository;
import com.capstone.service.AchievementService;

@RestController
@RequestMapping("/api/studygroups")
public class StudyGroupController {

    private static final Logger logger = LoggerFactory.getLogger(StudyGroupController.class);

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private AchievementService achievementService; 

    @Autowired
    private UserRepository userRepository; 

    @PostMapping("/add")
    public ResponseEntity<?> createStudyGroup(@RequestBody StudyGroup studyGroup, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User details not available.");
        }
        String creatorEmail = userDetails.getUsername();
        studyGroup.setOwner(creatorEmail); // Set the owner explicitly

        if (studyGroup.getMemberIds() == null) {
            studyGroup.setMemberIds(new java.util.ArrayList<>());
        }
        if (!studyGroup.getMemberIds().contains(creatorEmail)) {
            studyGroup.getMemberIds().add(creatorEmail);
        }

        
        try {
            logger.info("Attempting to save study group with name: {}", studyGroup.getName());
            StudyGroup savedGroup = studyGroupRepository.save(studyGroup);
            logger.info("Successfully saved study group with ID: {}", savedGroup.getId());
            // --- Achievement Check ---
            Optional<User> userOpt = userRepository.findByEmail(creatorEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                int newCount = user.incrementStudyGroupsCreated();
                userRepository.save(user); // Save updated count
                achievementService.checkAndAwardAchievements(user.getId(), AchievementType.STUDY_GROUP_CREATED, newCount);
            } else {
                logger.warn("User {} not found after creating group {} for achievement tracking.", creatorEmail, savedGroup.getId());
            }

            return ResponseEntity.ok(savedGroup); 
        } catch (Exception e) {
            logger.error("!!! Failed to save study group with name: {}", studyGroup.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to create study group due to database error.");
        }
    }

    @GetMapping("/all")
    public List<StudyGroup> getAllStudyGroups() {
        return studyGroupRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyGroupById(@PathVariable String id) {
        Optional<StudyGroup> group = studyGroupRepository.findById(id);
        if (group.isPresent()) {
            return ResponseEntity.ok(group.get());
        } else {
            logger.warn("Study group with ID: {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Study group not found with ID: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudyGroup(@PathVariable String id, @RequestBody StudyGroup studyGroupDetails) {
        return studyGroupRepository.findById(id).map(existingGroup -> {
            logger.info("Attempting to update study group with ID: {}", id);
            existingGroup.setName(studyGroupDetails.getName());
            existingGroup.setDescription(studyGroupDetails.getDescription());
            existingGroup.setMemberIds(studyGroupDetails.getMemberIds());
            try {
                StudyGroup updatedGroup = studyGroupRepository.save(existingGroup);
                 logger.info("Successfully updated study group with ID: {}", updatedGroup.getId());
                return ResponseEntity.ok(updatedGroup);
            } catch (Exception e) {
                 logger.error("!!! Failed to update study group with ID: {}", id, e);
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                      .body("Failed to update study group due to database error.");
            }
        }).orElseGet(() -> {
             logger.warn("Study group with ID: {} not found for update", id);
             return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                  .body("Study group not found with ID: " + id);
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyGroup(@PathVariable String id) {
         logger.info("Attempting to delete study group with ID: {}", id);
        try {
             if (studyGroupRepository.existsById(id)) {
                studyGroupRepository.deleteById(id);
                logger.info("Successfully deleted study group with ID: {}", id);
                return ResponseEntity.ok().body("Study group deleted successfully.");
             } else {
                 logger.warn("Study group with ID: {} not found for deletion", id);
                 return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                      .body("Study group not found with ID: " + id);
             }
        } catch (Exception e) {
             logger.error("!!! Failed to delete study group with ID: {}", id, e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body("Failed to delete study group due to database error.");
        }
    }

    @GetMapping("/joined/{email}")
    public ResponseEntity<?> getJoinedStudyGroups(@PathVariable String email,
                                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        if (userDetails == null || !userDetails.getUsername().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        try {
            List<StudyGroup> groups = studyGroupRepository.findByMemberIdsContaining(email);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error retrieving joined study groups for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error retrieving joined study groups.");
        }
    }

}
