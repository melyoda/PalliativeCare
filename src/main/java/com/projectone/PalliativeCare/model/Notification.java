package com.projectone.PalliativeCare.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;

    @NotBlank
    private String userId; // Target user

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private NotificationType type;

    private String relatedTopicId; // Optional: for topic-related notifications
    private String relatedPostId;  // Optional: for post-related notifications

    private boolean read = false;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt; // Optional: for temporary notifications

//    public enum NotificationType {
//        TOPIC_REGISTRATION,
//        TOPIC_UNREGISTRATION,
//        TOPIC_UPDATE,
//        NEW_POST,
//        NEW_COMMENT,
//        HELP_RESPONSE,
//        SYSTEM_ALERT,
//        DOCTOR_MESSAGE
//    }
}