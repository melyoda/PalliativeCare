package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.dto.ChatConversationDTO;
import com.projectone.PalliativeCare.model.ChatMessage;
import com.projectone.PalliativeCare.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatConversationDTO>> getChatConversations(Authentication authentication) {
        // Use authentication.getName() which safely gets the username (email)
        String currentUserEmail = authentication.getName();
        List<ChatConversationDTO> conversations = chatService.getConversationsForUser(currentUserEmail);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String roomId) {
        List<ChatMessage> messages = chatService.getChatMessagesByRoomId(roomId);
        return ResponseEntity.ok(messages);
    }

}