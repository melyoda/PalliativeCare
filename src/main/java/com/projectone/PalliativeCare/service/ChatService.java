package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.ChatConversationDTO;
import com.projectone.PalliativeCare.model.ChatMessage;
import com.projectone.PalliativeCare.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;
    // You would also inject a UserRepository to get participant names
    // private final UserRepository userRepository;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        // Save the message to MongoDB
        return messageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatMessagesByRoomId(String roomId) {
        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    public List<ChatConversationDTO> getConversationsForUser(String username) {
        // THIS IS A SIMPLIFIED LOGIC. For production, you should have a separate
        // 'ChatRoom' entity that explicitly lists participants for efficiency.

        // 1. Find all messages sent by the user to get involved roomIds
        List<ChatMessage> sentMessages = messageRepository.findBySender(username);

        // 2. Get all unique room IDs from these messages
        List<String> roomIds = sentMessages.stream()
                .map(ChatMessage::getRoomId)
                .distinct()
                .toList();

        // 3. For each room, create a DTO with the last message and other participant's info
        return roomIds.stream().map(roomId -> {
            // Find the other participant's username from the roomId
            // Assumes roomId format is "user1_user2" sorted alphabetically
            String[] participants = roomId.split("_");
            String otherParticipantId = participants[0].equals(username) ? participants[1] : participants[0];

            // Fetch the last message for the conversation preview
            ChatMessage lastMessage = messageRepository.findTopByRoomIdOrderByTimestampDesc(roomId)
                    .orElse(null);

            return ChatConversationDTO.builder()
                    .roomId(roomId)
                    .otherParticipantId(otherParticipantId)
                    .otherParticipantName("Dr. " + otherParticipantId) // Fetch real name from UserRepository
                    .lastMessage(lastMessage != null ? lastMessage.getContent() : "No messages yet")
                    .timestamp(lastMessage != null ? lastMessage.getTimestamp() : null)
                    .unreadCount(0) // Logic for unread count would be needed here
                    .build();
        }).collect(Collectors.toList());
    }
}