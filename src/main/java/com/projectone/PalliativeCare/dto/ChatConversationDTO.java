package com.projectone.PalliativeCare.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatConversationDTO {
    private String roomId;
    private String otherParticipantName;
    private String otherParticipantId;
    private String lastMessage;
    private LocalDateTime timestamp;
    private long unreadCount;
}