package com.projectone.PalliativeCare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicInfoDTO {
    private String topicId;
    private String topicName;

    private String topicDescription; // Optional
    private String topicLogoUrl; // Optional
}
