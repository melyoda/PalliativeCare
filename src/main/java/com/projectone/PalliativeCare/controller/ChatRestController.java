package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.dto.ChatConversationDTO;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
// Import your Spring Security principal object if you use it
// import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatConversationDTO>> getChatConversations() {
        String currentUsername = ((UserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();

        List<ChatConversationDTO> conversations = chatService.getConversationsForUser(currentUsername);
        return ResponseEntity.ok(conversations);
    }

}