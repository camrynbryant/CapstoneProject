package com.capstone.controller;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import com.capstone.service.ProfilePictureStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
public class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProfilePictureStorageService storageService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setProfilePictureUrl("http://example.com/pic.jpg");
        testUser.setStudyInterests(Set.of("Math", "Science"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetProfilePicture() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/profile-picture")
                        .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.pictureUrl").value("http://example.com/pic.jpg"));
    }

    @Test
    void testGetProfilePictureUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile-picture")
                        .with(csrf()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateProfilePictureSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image data".getBytes());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(storageService.storeFile(any())).thenReturn("http://example.com/new-pic.jpg");

        mockMvc.perform(multipart("/api/users/profile-picture")
                        .file(file)
                        .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.pictureUrl").value("http://example.com/new-pic.jpg"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateProfilePictureFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image data".getBytes());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(storageService.storeFile(any())).thenThrow(new IOException("Failed to store file"));

        mockMvc.perform(multipart("/api/users/profile-picture")
                        .file(file)
                        .with(csrf()))
               .andExpect(status().isInternalServerError())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("Error updating profile picture")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetStudyInterestsSuccess() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/test@example.com/interests")
                        .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.studyInterests").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateStudyInterests() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        String json = "[\"History\", \"Biology\"]";

        mockMvc.perform(put("/api/users/test@example.com/interests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.studyInterests").isArray());
    }
}
