package com.capstone.controller;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import com.capstone.service.ProfilePictureStorageService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProfilePictureStorageService storageService;

    @PostMapping("/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@RequestParam("file") MultipartFile file,
                                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        try {
            String fileUrl = storageService.storeFile(file);
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("pictureUrl", fileUrl));
        } catch (IOException ex) {
            return ResponseEntity.status(500).body("Error updating profile picture: " + ex.getMessage());
        }
    }

    @GetMapping("/profile-picture")
    public ResponseEntity<?> getProfilePicture(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        String pictureUrl = user.getProfilePictureUrl();
        return ResponseEntity.ok(Map.of("pictureUrl", pictureUrl));
    }
    
    @GetMapping("/{email}/interests")
    public ResponseEntity<?> getStudyInterests(@PathVariable String email,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        if (userDetails == null || !userDetails.getUsername().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Set<String> interests = optUser.get().getStudyInterests();
        return ResponseEntity.ok(Map.of("studyInterests", interests));
    }
    
    @PutMapping("/{email}/interests")
    public ResponseEntity<?> updateStudyInterests(@PathVariable String email,
    @RequestBody Set<String> interests,
    @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        if (userDetails == null || !userDetails.getUsername().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = optUser.get();
        user.setStudyInterests(interests);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("studyInterests", user.getStudyInterests()));
    }
}
