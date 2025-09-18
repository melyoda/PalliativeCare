package com.projectone.PalliativeCare.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "chatMessages") // <-- Tells Spring to save this to the "chatMessages" collection
public class ChatMessage {

    @Id
    private String id;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    private MessageType type;
    private String content;
    private String sender;
    private String roomId;
    private LocalDateTime timestamp;
}