package com.projectone.PalliativeCare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "topics")
public class Topic {

    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    private String description;

    private String logoUrl;

    private List<Resources> resources; // based on id I think

    private List<String> registeredUsers; //userRef

    private String createdBy; //based on user id / userRef only this user can update the topic

    private LocalDateTime creationDate;

    private LocalDateTime modifiedDate;

}
