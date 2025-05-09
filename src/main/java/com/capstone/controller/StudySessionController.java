package com.capstone.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.models.StudyGroup;
import com.capstone.models.StudySession;
import com.capstone.models.User;
import com.capstone.models.enums.AchievementType;
import com.capstone.repository.StudyGroupRepository;
import com.capstone.repository.StudySessionRepository;
import com.capstone.repository.UserRepository;
import com.capstone.service.AchievementService;
import com.capstone.service.NotificationService;

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

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository; 

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public StudySession createSession(@RequestBody StudySession session, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email not available from token");
        }
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

        // --- Achievement Check ---
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int newCount = user.incrementStudySessionsCreated();
            userRepository.save(user); // Save updated count
            achievementService.checkAndAwardAchievements(user.getId(), AchievementType.STUDY_SESSION_CREATED, newCount);
        } else {
            logger.warn("User {} not found after creating session {} for achievement tracking.", userEmail, savedSession.getId());
        }

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
        String userEmail = userDetails.getUsername();
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email not available from token");
        }
        boolean isMember = studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, userEmail);
        if (!isMember) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of this group to view its sessions.");
        }
        return sessionRepository.findByGroupId(groupId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public StudySession updateSession(@PathVariable String id, @RequestBody StudySession updatedSession, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email not available from token");
        }
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
        String userEmail = userDetails.getUsername();
        StudySession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study session not found"));
        if (userEmail == null) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email not available from token");
        }
        boolean isOwner = session.getCreatedBy().equals(userEmail);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or an admin can delete this session");
        }
        sessionRepository.deleteById(id);
    }

    @PutMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public StudySession joinSession(@PathVariable String id, @RequestParam String userId, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email not available from token");
        }
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

            boolean newlyJoined = false; // Flag to check if user was actually added
            if (session.getParticipantIds() == null) {
                session.setParticipantIds(new ArrayList<>());
            }
            if (!session.getParticipantIds().contains(userEmail)) { // Use email
                session.getParticipantIds().add(userEmail); // Use email
                newlyJoined = true;
            }

            StudySession updatedSession = sessionRepository.save(session);

            // --- Achievement Check ---
            if (newlyJoined) {
                Optional<User> userOpt = userRepository.findByEmail(userEmail);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    int newCount = user.incrementStudySessionsJoined();
                    userRepository.save(user); // Save updated count
                    achievementService.checkAndAwardAchievements(user.getId(), AchievementType.STUDY_SESSION_JOINED, newCount);
                } else {
                    logger.warn("User {} not found after joining session {} for achievement tracking.", userEmail, updatedSession.getId());
                }
            }

            return updatedSession;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study session not found"));
    }

    @PutMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public StudySession leaveSession(@PathVariable String id, @RequestParam String userId, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User email not available from token");
        }
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
