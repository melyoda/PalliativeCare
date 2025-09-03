package com.projectone.PalliativeCare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private String commentId;
    private String text;
    private LocalDateTime timestamp;
    private LocalDateTime lastEdited;
    private AuthorDTO author; // Enriched author info
}