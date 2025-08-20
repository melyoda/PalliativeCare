package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.TopicDTO;
import com.projectone.PalliativeCare.exception.FileUploadException;
import com.projectone.PalliativeCare.exception.InvalidRequestException;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.exception.UserAlreadyExistsException;
import com.projectone.PalliativeCare.model.*;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class TopicServices {

    private final TopicRepository topicRepo;
    private final UserRepository userRepo;

    @Autowired
    private ActivityService activityService;

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

//    public void registerUserToTopic(String topicId) {
//        // Get the current user's ID from the security context
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication().getPrincipal();
//        User currentUser = userRepo.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        String userId = currentUser.getId();
//
//        // Find the topic
//        Topic topic = topicRepo.findById(topicId)
//                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
//
//        // Add the user's ID to the list (if not already present)
//        if (!topic.getRegisteredUsers().contains(userId)) {
//            topic.getRegisteredUsers().add(userId);
//            topicRepo.save(topic);
//            //log
//            activityService.logActivity(userId, ActivityType.TOPIC_REGISTER, topicId, topic.getTitle());
//        } else {
//            // Optional: throw an exception if user is already registered
//            throw new UserAlreadyExistsException("User is already registered for this topic.");
//        }
//    }
//
//    public void unregisterUserFromTopic(String topicId) {
//        // Get the current user's ID from the security context
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        User currentUser = userRepo.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        String userId = currentUser.getId();
//
//        // Find the topic
//        Topic topic = topicRepo.findById(topicId)
//                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
//
//        // Remove the user's ID from the list
//        boolean removed = topic.getRegisteredUsers().remove(userId);
//        if (removed) {
//            topicRepo.save(topic);
//            //log
//            activityService.logActivity(userId, ActivityType.TOPIC_UNREGISTER, topicId, topic.getTitle());
//        } else {
//            throw new InvalidRequestException("User was not registered for this topic.");
//        }
//    }

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
                        ResourceType type = determineResourceType(Objects.requireNonNull(file.getOriginalFilename()));
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

        activityService.logActivity(creator.getId(), ActivityType.TOPIC_CREATE, topic.getId(), topic.getTitle());
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
            Path filePath = uploadPath.resolve(Objects.requireNonNull(file.getOriginalFilename()));

            // Save the file
            file.transferTo(filePath);

            // Return the path as a string
            return filePath.toString();

        } catch (IOException e) {
//            e.printStackTrace();
            throw new FileUploadException("File upload failed for " + file.getOriginalFilename() +" "+e.getMessage());
        }
    }

    private ResourceType determineResourceType(String filename) {
        if (filename.endsWith(".mp4") || filename.endsWith(".mov")) return ResourceType.VIDEO;
        if (filename.endsWith(".jpg") || filename.endsWith(".png")) return ResourceType.INFOGRAPHIC;
        return ResourceType.TEXT; // default
    }

}



