package com.capstone.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.repository.StudyGroupRepository;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private StudyGroupRepository groupRepository;

    @InjectMocks
    private FileStorageService fileStorageService;

    private final String GROUP_ID = "group-basic";
    private final String MEMBER_EMAIL = "member@example.com";
    private final String NON_MEMBER_EMAIL = "nonmember@example.com";

    @Test
    void checkUserMembership_WhenUserIsMember_ShouldPass() {
        when(groupRepository.existsByIdAndMemberIdsContaining(GROUP_ID, MEMBER_EMAIL))
                .thenReturn(true);

        assertDoesNotThrow(() -> fileStorageService.checkUserMembership(GROUP_ID, MEMBER_EMAIL));

        verify(groupRepository, times(1)).existsByIdAndMemberIdsContaining(GROUP_ID, MEMBER_EMAIL);
    }

    @Test
    void checkUserMembership_WhenUserIsNotMember_ShouldThrowForbidden() {
        when(groupRepository.existsByIdAndMemberIdsContaining(GROUP_ID, NON_MEMBER_EMAIL))
                .thenReturn(false);

        assertThatThrownBy(() -> fileStorageService.checkUserMembership(GROUP_ID, NON_MEMBER_EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);

        verify(groupRepository, times(1)).existsByIdAndMemberIdsContaining(GROUP_ID, NON_MEMBER_EMAIL);
    }
}