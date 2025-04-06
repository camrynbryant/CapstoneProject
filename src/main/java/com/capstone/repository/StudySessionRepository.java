package com.capstone.repository;

import com.capstone.models.StudySession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StudySessionRepository extends MongoRepository<StudySession, String> {
    List<StudySession> findByGroupId(String groupId);

    @Query("{ '$or': [ { 'topic': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<StudySession> searchByKeyword(String keyword);

        List<StudySession> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

}
