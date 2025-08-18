package com.projectone.PalliativeCare.dto;

import com.projectone.PalliativeCare.model.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDTO {

    @NotBlank
    private String title;
    @NotBlank
        private String description;

    private MultipartFile logo; // the logo file
    private List<MultipartFile> resources; // videos, infographics, etc.
}
