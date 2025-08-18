package com.projectone.PalliativeCare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "topic_resources")
public class Resources {

    @Id
    private String id;

    private String Type; //TEXT, VIDEO, INFOGRAPHIC, PDF, etc.

    private String content;//Text or URL




//    private String relatedTopic; // do I need this ?
}
