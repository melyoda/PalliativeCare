package com.projectone.PalliativeCare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
//    private String topicId;
    private List<MultipartFile> resources; // videos, infographics, etc.
}