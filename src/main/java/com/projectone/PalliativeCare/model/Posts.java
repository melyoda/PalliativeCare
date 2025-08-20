package com.projectone.PalliativeCare.model;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Posts {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @Indexed
    private String topicId;  // Reference to parent topic

    private String createdBy; // Doctor who created the post

    private String modifiedBy; // Last user who edited

    private LocalDateTime creationDate;

    private LocalDateTime modificationDate;

    // Using nested objects for comments instead of Map for better querying
    private List<Comment> comments = new ArrayList<>();

    // Nested Comment class
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Comment {

        private String userId;

        private String userDisplayName; // Optional: for showing names without extra queries

        private String text;

        private LocalDateTime timestamp;

        private LocalDateTime lastEdited;
    }

    // Helper methods
    public void addComment(String userId, String userDisplayName, String text) {
        comments.add(Comment.builder()
                .userId(userId)
                .userDisplayName(userDisplayName)
                .text(text)
                .timestamp(LocalDateTime.now())
                .build());
    }

}


