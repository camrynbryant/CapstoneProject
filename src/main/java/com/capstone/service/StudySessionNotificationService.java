package com.capstone.service;

import com.capstone.models.StudySession;
import com.capstone.repository.StudySessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudySessionNotificationService {
    @Autowired
    private StudySessionRepository sessionRepository;
    
    @Autowired
    private NotificationService notificationService;

    private Set<String> notifiedSessionIds = new HashSet<>();

    @Scheduled(fixedRate = 60000) 
    public void notifyUpcomingSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesFromNow = now.plusMinutes(30);
        LocalDateTime twentyNineMinutesFromNow = now.plusMinutes(29);

        List<StudySession> upcomingSessions = sessionRepository
            .findByStartTimeBetween(twentyNineMinutesFromNow, thirtyMinutesFromNow);

        for (StudySession session : upcomingSessions) {
            if (!notifiedSessionIds.contains(session.getId())) {
                String message = "Your study group is about to start a study session '" + 
                               session.getTopic() + "' in less than 30 minutes! Be ready to join!";
                if (session.getParticipantIds() != null) {
                    notificationService.sendNotificationToUsers(session.getParticipantIds(), message);
                }
                notifiedSessionIds.add(session.getId());
            }
        }
    }
}
