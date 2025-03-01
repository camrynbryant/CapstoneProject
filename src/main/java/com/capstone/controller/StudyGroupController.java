package com.capstone.controller;

import com.capstone.models.StudyGroup;
import com.capstone.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/studygroups")
public class StudyGroupController {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    // Create a new study group
    @PostMapping("/add")
    public StudyGroup createStudyGroup(@RequestBody StudyGroup studyGroup) {
        return studyGroupRepository.save(studyGroup);
    }

    // Get all study groups
    @GetMapping("/all")
    public List<StudyGroup> getAllStudyGroups() {
        return studyGroupRepository.findAll();
    }

    // Get a study group by ID
    @GetMapping("/{id}")
    public StudyGroup getStudyGroupById(@PathVariable String id) {
        Optional<StudyGroup> group = studyGroupRepository.findById(id);
        return group.orElse(null);
    }

    // Update a study group
    @PutMapping("/{id}")
    public StudyGroup updateStudyGroup(@PathVariable String id, @RequestBody StudyGroup studyGroup) {
        return studyGroupRepository.findById(id).map(existingGroup -> {
            existingGroup.setName(studyGroup.getName());
            existingGroup.setDescription(studyGroup.getDescription());
            existingGroup.setMemberIds(studyGroup.getMemberIds());
            return studyGroupRepository.save(existingGroup);
        }).orElse(null);
    }

    // Delete a study group
    @DeleteMapping("/{id}")
    public void deleteStudyGroup(@PathVariable String id) {
        studyGroupRepository.deleteById(id);
    }
}
