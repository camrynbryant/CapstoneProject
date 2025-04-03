package com.capstone.controller;

import com.capstone.models.StudyGroup;
import com.capstone.repository.StudyGroupRepository;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; 
import org.springframework.http.HttpStatus; 
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/studygroups")
public class StudyGroupController {

    private static final Logger logger = LoggerFactory.getLogger(StudyGroupController.class);

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @PostMapping("/add")
    public ResponseEntity<?> createStudyGroup(@RequestBody StudyGroup studyGroup) {
        try {
            logger.info("Attempting to save study group with name: {}", studyGroup.getName());
            StudyGroup savedGroup = studyGroupRepository.save(studyGroup);
            logger.info("Successfully saved study group with ID: {}", savedGroup.getId());
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
}
