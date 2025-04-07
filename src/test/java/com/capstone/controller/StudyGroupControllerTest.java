package com.capstone.controller;

import com.capstone.models.StudyGroup;
import com.capstone.repository.StudyGroupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudyGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private StudyGroup group1;
    private StudyGroup group2;
    private String testUserEmail = "owner@example.com";

    @BeforeEach
    void setUp() {
        group1 = new StudyGroup("Group 1", "Desc 1", testUserEmail, new ArrayList<>(List.of(testUserEmail)));
        group1.setId("group1");
        group2 = new StudyGroup("Group 2", "Desc 2", "other@example.com", new ArrayList<>(List.of("other@example.com")));
        group2.setId("group2");
    }

    @Test
    @WithMockUser
    void createStudyGroup_shouldReturnCreatedGroup() throws Exception {
        StudyGroup newGroup = new StudyGroup("New Group", "New Desc", testUserEmail, new ArrayList<>(List.of(testUserEmail)));
        StudyGroup savedGroup = new StudyGroup("New Group", "New Desc", testUserEmail, new ArrayList<>(List.of(testUserEmail)));
        savedGroup.setId("newId");
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);

        mockMvc.perform(post("/api/studygroups/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("newId"))
                .andExpect(jsonPath("$.name").value("New Group"));
    }

    @Test
    @WithMockUser
    void createStudyGroup_whenDbError_shouldReturnInternalServerError() throws Exception {
        StudyGroup newGroup = new StudyGroup("New Group", "New Desc", testUserEmail, new ArrayList<>(List.of(testUserEmail)));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/api/studygroups/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGroup)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to create study group due to database error."));
    }

    @Test
    @WithMockUser
    void getAllStudyGroups_shouldReturnListOfGroups() throws Exception {
        when(studyGroupRepository.findAll()).thenReturn(Arrays.asList(group1, group2));

        mockMvc.perform(get("/api/studygroups/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("group1"))
                .andExpect(jsonPath("$[1].id").value("group2"));
    }

    @Test
    @WithMockUser
    void getAllStudyGroups_whenNoGroups_shouldReturnEmptyList() throws Exception {
        when(studyGroupRepository.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/studygroups/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    void getStudyGroupById_whenExists_shouldReturnGroup() throws Exception {
        when(studyGroupRepository.findById("group1")).thenReturn(Optional.of(group1));

        mockMvc.perform(get("/api/studygroups/{id}", "group1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("group1"))
                .andExpect(jsonPath("$.name").value("Group 1"));
    }

    @Test
    @WithMockUser
    void getStudyGroupById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(studyGroupRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/studygroups/{id}", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Study group not found with ID: nonexistent"));
    }

    @Test
    @WithMockUser
    void updateStudyGroup_whenExists_shouldReturnUpdatedGroup() throws Exception {
        StudyGroup updatedDetails = new StudyGroup("Updated Name", "Updated Desc", group1.getOwner(), group1.getMemberIds());
        updatedDetails.setId("group1");
        when(studyGroupRepository.findById("group1")).thenReturn(Optional.of(group1));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(updatedDetails);

        mockMvc.perform(put("/api/studygroups/{id}", "group1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("group1"))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

     @Test
    @WithMockUser
    void updateStudyGroup_whenNotFound_shouldReturnNotFound() throws Exception {
        StudyGroup updatedDetails = new StudyGroup("Updated Name", "Updated Desc", testUserEmail, new ArrayList<>());
        when(studyGroupRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/studygroups/{id}", "nonexistent")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Study group not found with ID: nonexistent"));
    }

    @Test
    @WithMockUser
    void deleteStudyGroup_whenExists_shouldReturnOk() throws Exception {
        when(studyGroupRepository.existsById("group1")).thenReturn(true);
        doNothing().when(studyGroupRepository).deleteById("group1");

        mockMvc.perform(delete("/api/studygroups/{id}", "group1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Study group deleted successfully."));

        verify(studyGroupRepository).deleteById("group1");
    }

    @Test
    @WithMockUser
    void deleteStudyGroup_whenNotFound_shouldReturnNotFound() throws Exception {
        when(studyGroupRepository.existsById("nonexistent")).thenReturn(false);

        mockMvc.perform(delete("/api/studygroups/{id}", "nonexistent")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Study group not found with ID: nonexistent"));
    }
}
