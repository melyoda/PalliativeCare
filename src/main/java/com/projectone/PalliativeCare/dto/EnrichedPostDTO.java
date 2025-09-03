package com.projectone.PalliativeCare.dto;

import com.projectone.PalliativeCare.model.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedPostDTO {
    private String postId;
    private String title;
    private String content;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;

    // Enriched information
    private AuthorDTO author;
    private TopicInfoDTO topic;
    private List<Resource> resources;
    private List<CommentResponseDTO> comments;

    // Statistics (optional)
    private int commentCount;
    private boolean isLiked; // If you add likes later
    private int likeCount;   // If you add likes later
}