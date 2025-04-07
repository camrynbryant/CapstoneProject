package com.capstone.controller;

import com.capstone.models.StudySession;
import com.capstone.models.StudyGroup;
import com.capstone.service.NotificationService;
import com.capstone.repository.StudySessionRepository;
import com.capstone.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 


import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {

    private static final Logger logger = LoggerFactory.getLogger(StudySessionController.class);


    @Autowired
    private StudySessionRepository sessionRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public StudySession createSession(@RequestBody StudySession session, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String userEmail = userDetails.getUsername();
        String groupId = session.getGroupId();
        if (groupId == null || groupId.trim().isEmpty()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group ID is required to create a session");
        }
        boolean isMember = studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, userEmail);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of this group to create a session.");
        }

        session.setCreatedBy(userEmail);
        StudySession savedSession = sessionRepository.save(session);

        try {
            StudyGroup group = studyGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study group not found for notification"));

            List<String> memberIds = group.getMemberIds();

            if (memberIds != null && !memberIds.isEmpty()) {
                String message = "A new study session '" + savedSession.getTopic() + "' has been created in your group: " + group.getName();
                notificationService.sendNotificationToUsers(memberIds, message);
            }
        } catch (Exception e) {
             logger.error("Failed to send session creation notification for group ID: {}", groupId, e);
        }


        return savedSession;
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public List<StudySession> getSessionsByGroup(@PathVariable String groupId, @AuthenticationPrincipal UserDetails userDetails) {
         if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String userEmail = userDetails.getUsername();
        boolean isMember = studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, userEmail);
        if (!isMember) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of this group to view its sessions.");
        }
        return sessionRepository.findByGroupId(groupId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public StudySession updateSession(@PathVariable String id, @RequestBody StudySession updatedSession, @AuthenticationPrincipal UserDetails userDetails) {
         if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
         String userEmail = userDetails.getUsername();
        return sessionRepository.findById(id).map(session -> {
            if (!session.getCreatedBy().equals(userEmail)) {
                 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the session creator can update this session");
            }
            session.setTopic(updatedSession.getTopic());
            session.setDescription(updatedSession.getDescription());
            session.setStartTime(updatedSession.getStartTime());
            session.setEndTime(updatedSession.getEndTime());
            session.setLocation(updatedSession.getLocation());
            session.setParticipantIds(updatedSession.getParticipantIds());
            return sessionRepository.save(session);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study session not found"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteSession(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        StudySession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study session not found"));
        if (userDetails == null || userDetails.getUsername() == null) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String userEmail = userDetails.getUsername();
        boolean isOwner = session.getCreatedBy().equals(userEmail);
        boolean isAdmin = userDetails.getAuthorities() != null && userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or an admin can delete this session");
        }
        sessionRepository.deleteById(id);
    }

    @PutMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public StudySession joinSession(@PathVariable String id, @RequestParam String userId, @AuthenticationPrincipal UserDetails userDetails) {
         if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
         String userEmail = userDetails.getUsername();
        if (!userId.equals(userEmail)) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot join a session for another user.");
        }
        return sessionRepository.findById(id).map(session -> {
            String groupId = session.getGroupId();
            if (groupId == null || !studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, userEmail)) {
                 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of the session's parent group.");
            }
            if (session.getParticipantIds() == null) {
                session.setParticipantIds(new ArrayList<>());
            }
            if (!session.getParticipantIds().contains(userId)) {
                session.getParticipantIds().add(userId);
            }
            return sessionRepository.save(session);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study session not found"));
    }

    @PutMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public StudySession leaveSession(@PathVariable String id, @RequestParam String userId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
       String userEmail = userDetails.getUsername();
       if (!userId.equals(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot leave a session for another user.");
       }
       return sessionRepository.findById(id).map(session -> {
            String groupId = session.getGroupId();
            if (groupId == null || !studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, userEmail)) {
                 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of the session's parent group.");
            }
            if (session.getParticipantIds() != null) {
                session.getParticipantIds().remove(userId);
            }
            return sessionRepository.save(session);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study session not found"));
    }
}
