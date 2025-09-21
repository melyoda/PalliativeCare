package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> { // Assuming ID is String
    // Find the very last message for a given room
    Optional<ChatMessage> findTopByRoomIdOrderByTimestampDesc(String roomId);

    // Find all rooms a user is part of (this logic can be tricky)
    // A better approach is to have a separate 'ChatRoom' collection that lists participants.
    // For now, we'll derive it, but this is not efficient for large datasets.
//    List<ChatMessage> findBySender(String sender);
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    // 👇 ADD THIS: Finds all unique room IDs that contain the user's ID string.
    // This is more efficient than fetching all messages.
    @Query(value = "{ 'roomId' : { '$regex' : ?0 } }", fields = "{ 'roomId' : 1 }")
    List<ChatMessage> findDistinctRoomIdByRoomIdContaining(String userId);
}
