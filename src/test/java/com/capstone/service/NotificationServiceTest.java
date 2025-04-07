package com.capstone.service;

import com.capstone.models.Notification;
import com.capstone.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepo;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMarkAsRead() {
        Notification mockNotification = new Notification();
        mockNotification.setId("notification1");
        mockNotification.setRead(false);

        when(notificationRepo.findById("notification1")).thenReturn(Optional.of(mockNotification));
        when(notificationRepo.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Notification updatedNotification = notificationService.markAsRead("notification1");
        
        System.out.println("Mark as read returned notification with id: " + updatedNotification.getId());

        assertTrue(updatedNotification.isRead());
        verify(notificationRepo, times(1)).save(mockNotification);

        System.out.println("testMarkAsRead passed: Notification marked as read.");
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepo.findById("invalidId")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notificationService.markAsRead("invalidId"));
    }
}
