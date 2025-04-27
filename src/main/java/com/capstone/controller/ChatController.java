package com.capstone.controller;

import com.capstone.models.ChatMessage;
import com.capstone.models.User;
import com.capstone.models.enums.MessageType;
import com.capstone.repository.ChatMessageRepository;
import com.capstone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, 
                          ChatMessageRepository chatMessageRepository, 
                          UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat/{groupId}")
    public void handleChatMessage(@DestinationVariable String groupId,
                                  @Payload ChatMessage message,
                                  Principal principal) {
        if (principal == null) {
            System.out.println("Principal is null, cannot broadcast message.");
            return;
        }
        System.out.println("Principal found, proceeding to broadcast message...");

        String userEmail = principal.getName();
        String userName = userRepository.findByEmail(userEmail)
                .map(User::getName)
                .orElse(userEmail);

        message.setSenderEmail(userEmail);
        message.setSenderName(userName);
        message.setGroupId(groupId);
        message.setType(MessageType.CHAT);
        message.setTimestamp(LocalDateTime.now());

        chatMessageRepository.save(message);

        // BUILD A CLEAN SIMPLE PAYLOAD
        Map<String, Object> payload = new HashMap<>();
        payload.put("senderName", userName);
        payload.put("senderEmail", userEmail);
        payload.put("content", message.getContent());
        payload.put("timestamp", message.getTimestamp().toString());
        payload.put("type", message.getType().toString());

        // SERVER-SIDE DEBUG LOG
        System.out.println("Broadcasting payload to /topic/group/" + groupId + ": " + payload);

        // BROADCAST TO SUBSCRIBERS
        messagingTemplate.convertAndSend("/topic/group/" + groupId, payload);
    }
}
