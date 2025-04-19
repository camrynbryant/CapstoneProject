package com.capstone.controller;

import com.capstone.models.ChatMessage;
import com.capstone.models.User;
import com.capstone.models.enums.MessageType;
import com.capstone.repository.ChatMessageRepository;
import com.capstone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatControllerTest {

    @InjectMocks
    private ChatController chatController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Principal principal;

    @Captor
    private ArgumentCaptor<ChatMessage> chatMessageCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleChatMessage_withValidInput_shouldSaveAndBroadcastMessage() {
        // Arrange
        String groupId = "abc123";
        String userEmail = "jane@example.com";
        String userName = "Jane Doe";

        ChatMessage incomingMessage = new ChatMessage();
        incomingMessage.setContent("Test message");
        incomingMessage.setType(null); // should default to CHAT

        when(principal.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(new User(userName, userEmail, "password123")));

        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // echo

        // Act
        chatController.handleChatMessage(groupId, incomingMessage, principal);

        // Assert
        verify(chatMessageRepository).save(chatMessageCaptor.capture());
        ChatMessage savedMessage = chatMessageCaptor.getValue();

        assertEquals(userEmail, savedMessage.getSenderEmail());
        assertEquals(userName, savedMessage.getSenderName());
        assertEquals(groupId, savedMessage.getGroupId());
        assertEquals(MessageType.CHAT, savedMessage.getType());
        assertNotNull(savedMessage.getTimestamp());

        verify(messagingTemplate).convertAndSend(
                eq("/topic/group/" + groupId),
                eq(savedMessage)
        );
    }

    @Test
    void testHandleChatMessage_withNullPrincipal_shouldDoNothing() {
        // Act
        chatController.handleChatMessage("someGroup", new ChatMessage(), null);

        // Assert
        verifyNoInteractions(userRepository, chatMessageRepository, messagingTemplate);
    }
}
