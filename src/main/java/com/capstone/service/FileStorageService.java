package com.capstone.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.models.StudyResource;
import com.capstone.repository.StudyGroupRepository;
import com.capstone.repository.StudyResourceRepository;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.gridfs.model.GridFSFile;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private StudyResourceRepository resourceRepository;

    @Autowired
    private StudyGroupRepository groupRepository;

    @Value("${app.upload.allowed-types}")
    private String allowedTypesString;

    @Value("${app.upload.max-size-bytes}")
    private long maxSizeBytes;

    public StudyResource storeFile(MultipartFile file, String groupId, String uploaderEmail) throws IOException, IllegalArgumentException {
        throw new UnsupportedOperationException("Use storeFile with sendNotification flag");
    }

    public StudyResource storeFile(MultipartFile file, String groupId, String uploaderEmail, boolean sendNotification) throws IOException, IllegalArgumentException {
        checkUserMembership(groupId, uploaderEmail);

        List<String> allowedTypes = Arrays.asList(allowedTypesString.split(","));
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            logger.warn("Upload rejected for user {}: Invalid file type '{}'. Allowed: {}", uploaderEmail, contentType, allowedTypesString);
            throw new IllegalArgumentException("Invalid file type for '" + file.getOriginalFilename() + "'. Allowed types: " + String.join(", ", allowedTypes));
        }

        if (file.getSize() > maxSizeBytes) {
            logger.warn("Upload rejected for user {}: File size {} exceeds limit {}", uploaderEmail, file.getSize(), maxSizeBytes);
            throw new IllegalArgumentException("File '" + file.getOriginalFilename() + "' size exceeds the limit of " + (maxSizeBytes / 1024 / 1024) + "MB.");
        }

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence " + filename);
        }
        if (filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty.");
        }

        ObjectId gridFsId;
        try {
            gridFsId = gridFsTemplate.store(
                file.getInputStream(),
                filename,
                file.getContentType(),
                new BasicDBObjectBuilder().add("groupId", groupId).add("uploaderEmail", uploaderEmail).get()
            );
        } catch (IOException e) {
            logger.error("Could not store file {} for user {} in group {}", filename, uploaderEmail, groupId, e);
            throw new IOException("Failed to store file '" + filename + "'.", e);
        }

        StudyResource resource = new StudyResource(
            filename,
            file.getContentType(),
            file.getSize(),
            uploaderEmail,
            groupId,
            gridFsId.toString()
        );
        StudyResource savedResource = resourceRepository.save(resource);
        logger.info("User {} successfully uploaded file {} (ID: {}) to group {}", uploaderEmail, filename, savedResource.getId(), groupId);

        return savedResource;
    }

    public void checkUserMembership(String groupId, String userEmail) throws SecurityException {
        if (!groupRepository.existsByIdAndMemberIdsContaining(groupId, userEmail)) {
            logger.warn("User {} attempted action on group {} without membership.", userEmail, groupId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this study group.");
        }
    }

    public Optional<GridFsResource> getFileByResourceId(String resourceId) {
        Optional<StudyResource> resourceOpt = resourceRepository.findById(resourceId);
        if (resourceOpt.isEmpty()) {
            logger.warn("Attempted to access non-existent resource with ID: {}", resourceId);
            return Optional.empty();
        }

        StudyResource resource = resourceOpt.get();
        GridFSFile gridFsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(resource.getGridFsId()))));

        if (gridFsFile == null) {
            logger.error("Metadata found for resource ID {} but GridFS file missing (GridFS ID: {})", resourceId, resource.getGridFsId());
            return Optional.empty();
        }

        return Optional.of(gridFsTemplate.getResource(gridFsFile));
    }

    public Optional<StudyResource> getResourceMetadata(String resourceId) {
        return resourceRepository.findById(resourceId);
    }

    public List<StudyResource> getFilesForGroup(String groupId, String userEmail) {
        checkUserMembership(groupId, userEmail);
        return resourceRepository.findByGroupIdOrderByUploadDateDesc(groupId);
    }

    public void deleteFile(String resourceId, String currentUserEmail) throws ResponseStatusException {
        StudyResource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with ID: " + resourceId));

        if (!resource.getUploaderEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this file.");
        }

        try {
            ObjectId gridFsObjectId = new ObjectId(resource.getGridFsId());
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(gridFsObjectId)));
            logger.info("Successfully deleted GridFS object with ID: {}", resource.getGridFsId());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid GridFS ID format '{}' found in metadata for resource ID {}", resource.getGridFsId(), resourceId, e);
        } catch (Exception e) {
            logger.error("Error deleting file from GridFS with ID {}: {}", resource.getGridFsId(), e.getMessage(), e);
        }

        try {
            resourceRepository.deleteById(resourceId);
            logger.info("Successfully deleted resource metadata with ID: {}", resourceId);
        } catch (Exception e) {
            logger.error("Error deleting resource metadata with ID {}: {}", resourceId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file metadata after deleting file data.", e);
        }
    }
}
