package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.ChatConversationDTO;
import com.projectone.PalliativeCare.model.ChatMessage;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.ChatMessageRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;
     private final UserRepository userRepository;

    public void saveMessage(ChatMessage chatMessage) {
        // Save the message to MongoDB
        messageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatMessagesByRoomId(String roomId) {
        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    public List<ChatConversationDTO> getConversationsForUser(String userEmail) {
        // 2. Find the current user by email to get their ID
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
        String currentUserId = currentUser.getId();

        // 3. Use the new, reliable repository method to find all room IDs
        List<String> roomIds = messageRepository.findDistinctRoomIdByRoomIdContaining(currentUserId).stream()
                .map(ChatMessage::getRoomId)
                .distinct()
                .toList();

        // 4. For each room, create a DTO with the last message and REAL participant info
        List<ChatConversationDTO> conversations = roomIds.stream().map(roomId -> {
            // Find the other participant's ID from the roomId
            String[] participants = roomId.split("_");
            String otherParticipantId = participants[0].equals(currentUserId) ? participants[1] : participants[0];

            // Fetch the last message for the conversation preview
            ChatMessage lastMessage = messageRepository.findTopByRoomIdOrderByTimestampDesc(roomId)
                    .orElse(null);

            // 5. Fetch the real user details for the other participant
            String otherParticipantName = userRepository.findById(otherParticipantId)
                    .map(user -> user.getFirstName() + " " + user.getLastName()) // Build the full name
                    .orElse("Unknown User"); // Fallback if user is not found

            return ChatConversationDTO.builder()
                    .roomId(roomId)
                    .otherParticipantId(otherParticipantId)
                    .otherParticipantName(otherParticipantName) // Use the real name
                    .lastMessage(lastMessage != null ? lastMessage.getContent() : "No messages yet")
                    .timestamp(lastMessage != null ? lastMessage.getTimestamp() : null)
                    .unreadCount(0) // Logic for unread count would be needed here
                    .build();
        }).sorted(Comparator.comparing(ChatConversationDTO::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());

        // 6. Sort conversations by the most recent message timestamp

        return conversations;
    }
}