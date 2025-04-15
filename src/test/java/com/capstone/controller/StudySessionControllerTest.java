package com.capstone.controller;

import com.capstone.config.SecurityConfig;
import com.capstone.models.StudyGroup;
import com.capstone.models.StudySession;
import com.capstone.repository.StudyGroupRepository;
import com.capstone.repository.StudySessionRepository;
import com.capstone.security.JwtAuthenticationFilter;
import com.capstone.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq; 
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(controllers = StudySessionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
public class StudySessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudySessionRepository sessionRepository;

    @MockBean
    private StudyGroupRepository studyGroupRepository;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private StudySession session1;
    private StudySession session2;
    private String testUserEmail = "test@example.com";
    private String otherUserEmail = "other@example.com";
    private String groupId = "group1";
    private StudyGroup testGroup;

    @BeforeEach
    public void setUp() {
         objectMapper.registerModule(new JavaTimeModule());
        session1 = new StudySession("s1", groupId, "Topic 1", "Desc 1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Loc 1", new ArrayList<>(List.of(testUserEmail)), testUserEmail);
        session2 = new StudySession("s2", groupId, "Topic 2", "Desc 2", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), "Loc 2", new ArrayList<>(), testUserEmail);
        testGroup = new StudyGroup("Group 1", "Desc", testUserEmail, new ArrayList<>(List.of(testUserEmail, otherUserEmail)));
        testGroup.setId(groupId);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void getSessionsByGroup_whenMember_shouldReturnSessions() throws Exception {
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, testUserEmail)).thenReturn(true);
        when(sessionRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(session1, session2));

        mockMvc.perform(get("/api/sessions/group/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[1].id").value("s2"));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void getSessionsByGroup_whenNotMember_shouldReturnForbidden() throws Exception {
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, otherUserEmail)).thenReturn(false);

        mockMvc.perform(get("/api/sessions/group/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getSessionsByGroup_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
         mockMvc.perform(get("/api/sessions/group/{groupId}", groupId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void createSession_whenMember_shouldReturnCreatedSessionAndNotify() throws Exception {
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, testUserEmail)).thenReturn(true);
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> {
            StudySession sessionToSave = invocation.getArgument(0);
            if(sessionToSave.getId() == null) sessionToSave.setId("newSessionId");
            sessionToSave.setCreatedBy(testUserEmail);
            return sessionToSave;
        });
        doNothing().when(notificationService).sendNotificationToUsers(any(), anyString());


        StudySession newSession = new StudySession(null, groupId, "New Topic", "New Desc", LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), "New Loc", new ArrayList<>(), null);

        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSession)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("newSessionId"))
                .andExpect(jsonPath("$.topic").value("New Topic"))
                .andExpect(jsonPath("$.createdBy").value(testUserEmail));

        verify(notificationService).sendNotificationToUsers(eq(testGroup.getMemberIds()), anyString());
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void createSession_whenNotMember_shouldReturnForbidden() throws Exception {
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, otherUserEmail)).thenReturn(false);
        StudySession newSession = new StudySession(null, groupId, "New Topic", "New Desc", LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), "New Loc", new ArrayList<>(), null);

        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSession)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void updateSession_whenCreator_shouldReturnUpdatedSession() throws Exception {
        session1.setCreatedBy(testUserEmail);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudySession updatedDetails = new StudySession("s1", groupId, "Updated Topic", "Updated Desc", session1.getStartTime(), session1.getEndTime(), "Updated Loc", session1.getParticipantIds(), testUserEmail);

        mockMvc.perform(put("/api/sessions/{id}", "s1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("Updated Topic"))
                .andExpect(jsonPath("$.description").value("Updated Desc"));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void updateSession_whenNotCreator_shouldReturnForbidden() throws Exception {
        session1.setCreatedBy(testUserEmail);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));
        StudySession updatedDetails = new StudySession("s1", groupId, "Updated Topic", "Updated Desc", session1.getStartTime(), session1.getEndTime(), "Updated Loc", session1.getParticipantIds(), testUserEmail);

        mockMvc.perform(put("/api/sessions/{id}", "s1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void updateSession_whenNotFound_shouldReturnNotFound() throws Exception {
         when(sessionRepository.findById("nonexistent")).thenReturn(Optional.empty());
         StudySession updatedDetails = new StudySession("nonexistent", groupId, "Updated Topic", "Updated Desc", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Updated Loc", new ArrayList<>(), testUserEmail);

         mockMvc.perform(put("/api/sessions/{id}", "nonexistent")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "test@example.com")
    public void deleteSession_whenCreator_shouldReturnOk() throws Exception {
        session1.setCreatedBy(testUserEmail);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));
        doNothing().when(sessionRepository).deleteById(anyString());

        mockMvc.perform(delete("/api/sessions/{id}", "s1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(sessionRepository).deleteById("s1");
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void deleteSession_whenNotCreator_shouldReturnForbidden() throws Exception {
        session1.setCreatedBy(testUserEmail);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));

        mockMvc.perform(delete("/api/sessions/{id}", "s1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

     @Test
    @WithMockUser(username = "test@example.com")
    public void deleteSession_whenNotFound_shouldReturnNotFound() throws Exception {
        when(sessionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/sessions/{id}", "nonexistent")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void joinSession_whenMemberAndSelf_shouldReturnUpdatedSession() throws Exception {
        session1.setParticipantIds(new ArrayList<>(List.of(testUserEmail)));
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, otherUserEmail)).thenReturn(true);
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> {
             StudySession savedSession = invocation.getArgument(0);
             if(!savedSession.getParticipantIds().contains(otherUserEmail)) {
                 savedSession.getParticipantIds().add(otherUserEmail);
             }
             return savedSession;
        });

        mockMvc.perform(put("/api/sessions/{id}/join", "s1")
                        .param("userId", otherUserEmail)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantIds", hasItem(otherUserEmail)));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void joinSession_whenNotSelf_shouldReturnForbidden() throws Exception {
         mockMvc.perform(put("/api/sessions/{id}/join", "s1")
                        .param("userId", otherUserEmail)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void joinSession_whenNotGroupMember_shouldReturnForbidden() throws Exception {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, otherUserEmail)).thenReturn(false);

        mockMvc.perform(put("/api/sessions/{id}/join", "s1")
                        .param("userId", otherUserEmail)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void leaveSession_whenMemberAndSelf_shouldReturnUpdatedSession() throws Exception {
        session1.setParticipantIds(new ArrayList<>(List.of(testUserEmail, otherUserEmail)));
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(session1));
        when(studyGroupRepository.existsByIdAndMemberIdsContaining(groupId, testUserEmail)).thenReturn(true);
        when(sessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> {
             StudySession savedSession = invocation.getArgument(0);
             savedSession.getParticipantIds().remove(testUserEmail);
             return savedSession;
        });

        mockMvc.perform(put("/api/sessions/{id}/leave", "s1")
                        .param("userId", testUserEmail)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantIds", not(hasItem(testUserEmail))))
                .andExpect(jsonPath("$.participantIds", hasItem(otherUserEmail)));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    public void leaveSession_whenNotSelf_shouldReturnForbidden() throws Exception {
         mockMvc.perform(put("/api/sessions/{id}/leave", "s1")
                        .param("userId", testUserEmail)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}
