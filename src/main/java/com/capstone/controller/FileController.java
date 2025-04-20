package com.capstone.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.models.StudyGroup;
import com.capstone.models.StudyResource;
import com.capstone.models.User;
import com.capstone.models.enums.AchievementType;
import com.capstone.repository.StudyGroupRepository;
import com.capstone.repository.UserRepository;
import com.capstone.service.AchievementService;
import com.capstone.service.FileStorageService; 
import com.capstone.service.NotificationService; 

@RestController
@RequestMapping("/api")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StudyGroupRepository groupRepository;

    @Autowired
    private AchievementService achievementService; 

    @Autowired
    private UserRepository userRepository; 

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }

    @PostMapping("/studygroups/{groupId}/files")
    public ResponseEntity<?> uploadFiles(@PathVariable String groupId, @RequestParam("files") MultipartFile[] files) {
        String uploaderEmail = getCurrentUserEmail();
        if (uploaderEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }

        if (files == null || files.length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No files selected for upload.");
        }

        Optional<User> userOpt = userRepository.findByEmail(uploaderEmail);
        User uploader = userOpt.orElse(null);
        if (uploader == null) {
             logger.warn("Uploader {} not found for achievement tracking during file upload.", uploaderEmail);
        }

        List<StudyResource> savedResources = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                StudyResource savedResource = fileStorageService.storeFile(file, groupId, uploaderEmail, false);
                savedResources.add(savedResource);

                if (uploader != null) {
                    int newCount = uploader.incrementFilesUploaded();
                    userRepository.save(uploader); 
                    achievementService.checkAndAwardAchievements(uploader.getId(), AchievementType.FILE_UPLOADED, newCount);
                }
            } catch (IllegalArgumentException | SecurityException e) {
                logger.warn("File upload validation failed for user {} file '{}': {}", uploaderEmail, file.getOriginalFilename(), e.getMessage());
                errorMessages.add("Failed to upload '" + file.getOriginalFilename() + "': " + e.getMessage());
            } catch (IOException e) {
                logger.error("File upload failed for user {} file '{}' due to storage error: {}", uploaderEmail, file.getOriginalFilename(), e.getMessage(), e);
                errorMessages.add("Could not upload '" + file.getOriginalFilename() + "' due to a server error.");
            } catch (Exception e) {
                logger.error("Unexpected error during file upload for user {} file '{}': {}", uploaderEmail, file.getOriginalFilename(), e.getMessage(), e);
                errorMessages.add("An unexpected error occurred while uploading '" + file.getOriginalFilename() + "'.");
            }
        }

        if (!savedResources.isEmpty()) {
            Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);
            if (groupOpt.isPresent()) {
                StudyGroup group = groupOpt.get();
                List<String> membersToNotify = group.getMemberIds().stream()
                        .filter(memberId -> !memberId.equals(uploaderEmail))
                        .collect(Collectors.toList());

                if (!membersToNotify.isEmpty()) {
                    String fileNames = savedResources.stream()
                            .map(StudyResource::getFilename)
                            .collect(Collectors.joining(", "));
                    if (fileNames.length() > 100) {
                        fileNames = fileNames.substring(0, 97) + "...";
                    }
                    String message = String.format("User %s uploaded %d new file(s) (%s) to the group '%s'.",
                            uploaderEmail, savedResources.size(), fileNames, group.getName());
                    notificationService.sendNotificationToUsers(membersToNotify, message);
                    logger.info("Sent aggregated notification to {} members for {} file uploads in group {}", membersToNotify.size(), savedResources.size(), groupId);
                }
            } else {
                logger.warn("Could not find group {} to send aggregated notification after file uploads.", groupId);
            }
        }

        if (savedResources.isEmpty() && !errorMessages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        } else if (!savedResources.isEmpty() && !errorMessages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new UploadResult(savedResources, errorMessages));
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResources);
        }
    }

    static class UploadResult {
        public List<StudyResource> successes;
        public List<String> failures;

        public UploadResult(List<StudyResource> successes, List<String> failures) {
            this.successes = successes;
            this.failures = failures;
        }
    }

    @GetMapping("/studygroups/{groupId}/files")
    public ResponseEntity<?> listFilesForGroup(@PathVariable String groupId) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }
        try {
            List<StudyResource> files = fileStorageService.getFilesForGroup(groupId, userEmail);
            return ResponseEntity.ok(files);
        } catch (SecurityException e) {
            logger.warn("Unauthorized attempt to list files for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error listing files for group {}: {}", groupId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not retrieve file list.");
        }
    }

    @GetMapping("/files/{resourceId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String resourceId) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<StudyResource> metadataOpt = fileStorageService.getResourceMetadata(resourceId);
        if (metadataOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        StudyResource metadata = metadataOpt.get();

        try {
            fileStorageService.checkUserMembership(metadata.getGroupId(), userEmail);
        } catch (SecurityException e) {
            logger.warn("Forbidden download attempt: User {} tried to download resource {} from group {}", userEmail, resourceId, metadata.getGroupId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<GridFsResource> fileResourceOpt = fileStorageService.getFileByResourceId(resourceId);
        if (fileResourceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GridFsResource fileResource = fileResourceOpt.get();
        String encodedFilename;
        try {
            encodedFilename = URLEncoder.encode(metadata.getFilename(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to URL encode filename: {}", metadata.getFilename(), e);
            encodedFilename = "downloaded_file";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(fileResource);
    }

    @DeleteMapping("/files/{resourceId}")
    public ResponseEntity<?> deleteFile(@PathVariable String resourceId) {
        String currentUserEmail = getCurrentUserEmail();
        if (currentUserEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }

        try {
            fileStorageService.deleteFile(resourceId, currentUserEmail);
            logger.info("User {} successfully deleted resource {}", currentUserEmail, resourceId);
            return ResponseEntity.ok().body("File deleted successfully.");
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Attempt to delete non-existent resource {} by user {}", resourceId, currentUserEmail);
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("Unauthorized delete attempt for resource {} by user {}", resourceId, currentUserEmail);
            } else {
                logger.error("Error deleting resource {} for user {}: Status {}, Reason: {}", resourceId, currentUserEmail, e.getStatusCode(), e.getReason(), e);
            }
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Unexpected error deleting resource {} for user {}: {}", resourceId, currentUserEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during file deletion.");
        }
    }
}