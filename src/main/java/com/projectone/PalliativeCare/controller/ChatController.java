package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.model.ChatMessage;
import com.projectone.PalliativeCare.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // Use constructor injection
public class ChatController {

    // Inject the template to send messages to specific destinations
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService; // Inject the service to save messages

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        String roomId = chatMessage.getRoomId();

        // Add username and roomId to the WebSocket session
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(java.time.LocalDateTime.now());

        // Broadcast the JOIN message ONLY to the specific room's topic
        messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        chatMessage.setTimestamp(java.time.LocalDateTime.now());

        // 1. Persist the message to the database
        chatService.saveMessage(chatMessage);

        // 2. Send the message ONLY to the specific room's topic
        // The roomId from the payload determines the destination
        messagingTemplate.convertAndSend("/topic/room/" + chatMessage.getRoomId(), chatMessage);
    }
}