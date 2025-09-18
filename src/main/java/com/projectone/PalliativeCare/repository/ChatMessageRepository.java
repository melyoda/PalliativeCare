package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> { // Assuming ID is String
    // Find the very last message for a given room
    Optional<ChatMessage> findTopByRoomIdOrderByTimestampDesc(String roomId);

    // Find all rooms a user is part of (this logic can be tricky)
    // A better approach is to have a separate 'ChatRoom' collection that lists participants.
    // For now, we'll derive it, but this is not efficient for large datasets.
    List<ChatMessage> findBySender(String sender);
}
