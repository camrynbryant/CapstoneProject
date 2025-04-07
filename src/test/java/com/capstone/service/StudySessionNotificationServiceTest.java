package com.capstone.service;

import com.capstone.models.StudySession;
import com.capstone.repository.StudySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StudySessionNotificationServiceTest {

    @Mock
    private StudySessionRepository sessionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StudySessionNotificationService studySessionNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNotifyUpcomingSessions() {
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesFromNow = now.plusMinutes(30);
        StudySession mockSession = new StudySession();
        mockSession.setId("session1");
        mockSession.setTopic("Math Study");
        mockSession.setParticipantIds(Collections.singletonList("user1"));
                mockSession.setStartTime(thirtyMinutesFromNow);


        when(sessionRepository.findByStartTimeBetween(any(), any()))
                .thenReturn(Collections.singletonList(mockSession));

        studySessionNotificationService.notifyUpcomingSessions();

        verify(notificationService, times(1))
                .sendNotificationToUsers(eq(Collections.singletonList("user1")),
                        eq("Your study group is about to start a study session 'Math Study' in less than 30 minutes! Be ready to join!"));
    }
}
