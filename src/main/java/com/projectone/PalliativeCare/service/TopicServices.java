package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.TopicDTO;
import com.projectone.PalliativeCare.model.Resource;
import com.projectone.PalliativeCare.model.ResourceType;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopicServices {

    private final TopicRepository topicRepo;
    private final UserRepository userRepo;

    public TopicServices(TopicRepository topicRepo, UserRepository userRepo) {
        this.topicRepo = topicRepo;
        this.userRepo = userRepo;
    }

    public List<Topic> listAllTopics() {
        return topicRepo.findAll();
    }

    public List<Topic> searchTopics(String keyword) {
        // Case-insensitive search on title
        return topicRepo.findByTitleContainingIgnoreCase(keyword);
    }



    /**
     * Creates A Topic based on the DTO
     * @param topicDTO, TopicDTO
     */
    public void createTopic(TopicDTO topicDTO) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        User creator  = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String logoUrl = null;
        if (topicDTO.getLogo() != null && !topicDTO.getLogo().isEmpty()) {
            logoUrl = saveFileLocally(topicDTO.getLogo());
        }

        /// Upload resources and create embedded Resource objects
        List<Resource> resources = List.of(); // empty list if none
        if (topicDTO.getResources() != null) {
            resources = topicDTO.getResources().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        String url = saveFileLocally(file);
                        ResourceType type = determineResourceType(file.getOriginalFilename());
                        return Resource.builder()
                                .type(type)
                                .contentUrl(url)
                                .build();
                    })
                    .toList();
        }


        Topic topic = Topic.builder()
                .title(topicDTO.getTitle())
                .description(topicDTO.getDescription())
                .logoUrl(logoUrl)
                .resources(resources)
                .registeredUsers(List.of())
                .createdBy(creator.getId())
                .creationDate(LocalDateTime.now())
                .build();

        topicRepo.save(topic);


    }

    private String saveFileLocally(MultipartFile file) {
        // Define the upload directory relative to the project's root
        String uploadDir = "Resources/";

        try {
            // Create a Path object for the directory
            Path uploadPath = Paths.get(uploadDir);

            // Create the directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // This creates parent directories as well
            }

            // Resolve the final file path
            Path filePath = uploadPath.resolve(file.getOriginalFilename());

            // Save the file
            file.transferTo(filePath);

            // Return the path as a string
            return filePath.toString();

        } catch (IOException e) {
            // It's good practice to log the full exception
            e.printStackTrace();
            throw new RuntimeException("File upload failed for " + file.getOriginalFilename(), e);
        }
    }

    private ResourceType determineResourceType(String filename) {
        if (filename.endsWith(".mp4") || filename.endsWith(".mov")) return ResourceType.VIDEO;
        if (filename.endsWith(".jpg") || filename.endsWith(".png")) return ResourceType.INFOGRAPHIC;
        return ResourceType.TEXT; // default
    }

}



