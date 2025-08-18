package com.projectone.PalliativeCare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    private ResourceType type; //TEXT, VIDEO, INFOGRAPHIC, PDF, etc.

    private String content; //text content (if applicable)

    private String contentUrl;
}
