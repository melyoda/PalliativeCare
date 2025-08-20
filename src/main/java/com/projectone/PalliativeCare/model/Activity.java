package com.projectone.PalliativeCare.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "activities")
public class Activity {

    @Id
    private String id;

    private String userId; // Who performed the action
    private ActivityType type; // What action was performed

    private String targetId; // The ID of the item being acted on (e.g., topicId, fileId)
    private String targetDescription; // Optional: e.g., the title of the topic or name of the file

    private LocalDateTime timestamp;
}

