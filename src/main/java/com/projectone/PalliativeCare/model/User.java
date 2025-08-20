package com.projectone.PalliativeCare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users") // this maps the class to the "users" collection
public class User {

    @Id
    private String id; // MongoDB ids are usually String (ObjectId hex string)

    @Indexed(unique = true) // email should be unique
    private String email;

    private String password;

    private String firstName;

    private String middleName;

    private String lastName;

    private String birthDate;

    private String mobile;

    private String address;

    private Role role;

    private List<String> registeredTopics; //Topics IDs I think

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public String getUsername() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + getUsername() + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
