package com.capstone.controller;

import com.capstone.models.ChatMessage;
import com.capstone.models.enums.MessageType;
import com.capstone.repository.ChatMessageRepository;
import com.capstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat/{groupId}")
    public void handleChatMessage(@DestinationVariable String groupId,
                                  @Payload ChatMessage message,
                                  Principal principal) {

        if (principal == null) {
            return;
        }

        String userEmail = principal.getName();
        String userName = userRepository.findByEmail(userEmail)
                .map(u -> u.getName())
                .orElse(userEmail);

        message.setSenderEmail(userEmail);
        message.setSenderName(userName);
        message.setTimestamp(LocalDateTime.now());
        message.setGroupId(groupId);
        message.setType(message.getType() != null ? message.getType() : MessageType.CHAT);

        ChatMessage saved = chatMessageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, saved);
    }
}