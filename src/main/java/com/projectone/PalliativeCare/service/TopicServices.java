package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.TopicDTO;
import com.projectone.PalliativeCare.exception.*;
import com.projectone.PalliativeCare.model.*;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import com.projectone.PalliativeCare.utils.StoreResources;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TopicServices {

    private final TopicRepository topicRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final ActivityService activityService;
    private final StoreResources storeResources;

    public List<Topic> listAllTopics() {
        return topicRepo.findAll();
    }

    public List<Topic> searchTopics(String keyword) {
        List<Topic> results = topicRepo.findByTitleContainingIgnoreCase(keyword);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No topics found matching: " + keyword);
        }
        return results;
    }

    /**
     * Creates A Topic based on the DTO
     * @param topicDTO, TopicDTO
     */
    public void createTopic(TopicDTO topicDTO) {
        User creator = getCurrentUser();

        String logoUrl = null;
        if (topicDTO.getLogo() != null && !topicDTO.getLogo().isEmpty()) {
            logoUrl = storeResources.saveFiles(topicDTO.getLogo());
        }

        Topic topic = Topic.builder()
                .title(topicDTO.getTitle())
                .description(topicDTO.getDescription())
                .logoUrl(logoUrl)
                .resources(getTopicResources(topicDTO))
                .registeredUsers(List.of())
                .createdBy(creator.getId())
                .creationDate(LocalDateTime.now())
                .build();

        topicRepo.save(topic);

        activityService.logActivity(
                creator.getId(),
                ActivityType.TOPIC_CREATE,
                topic.getId(),
                "Creation of topic: "+ topic.getTitle()
        );
    }

    public void deleteTopic(String topicId) {
        User currentUser = getCurrentUser();
        Topic topic = getCurrentTopic(topicId);

        if(!Objects.equals(currentUser.getId(), topic.getCreatedBy())) {
            throw new UnauthorizedActionException("You do not have permission to delete this topic");
        }
        activityService.logActivity(
                currentUser.getId(),
                ActivityType.TOPIC_DELETE,
                topic.getId(),
                "Deletion of topic : "+ topic.getTitle()
        );
        topicRepo.deleteById(topicId);
    }

    public void updateTopic(String topicId, TopicDTO topicDTO) {
        User currentUser = getCurrentUser();

        // Fetch the topic by id
        Topic existingTopic = getCurrentTopic(topicId);


        // Ensure only the creator can update
        if (!Objects.equals(currentUser.getId(), existingTopic.getCreatedBy())) {
            throw new UnauthorizedActionException("You do not have permission to update this Topic");
        }

        // Update title if provided
        if (topicDTO.getTitle() != null) {
            existingTopic.setTitle(topicDTO.getTitle());
        }

        // Update description if provided
        if (topicDTO.getDescription() != null) {
            existingTopic.setDescription(topicDTO.getDescription());
        }

        // Update logo if provided
        if (topicDTO.getLogo() != null && !topicDTO.getLogo().isEmpty()) {
            String logoUrl = storeResources.saveFiles(topicDTO.getLogo());
            existingTopic.setLogoUrl(logoUrl);
        }
        if (topicDTO.getResources() != null && !topicDTO.getResources().isEmpty()) {
            existingTopic.setResources(getTopicResources(topicDTO));
        }

        // Always update modified date
        existingTopic.setModifiedDate(LocalDateTime.now());

        // Save changes
        Topic updatedTopic = topicRepo.save(existingTopic);

        // Log activity
        activityService.logActivity(
                currentUser.getId(),
                ActivityType.TOPIC_EDIT,
                existingTopic.getId(),
                "Update of topic: " + existingTopic.getTitle()
        );

        // ✅ Send notification to all topic subscribers
        // ✅ Notify all subscribers about the update
        notificationService.sendToTopicSubscribers(
                topicId,
                "Topic Updated: " + updatedTopic.getTitle(),
                "The topic has been updated with new information",
                NotificationType.TOPIC_UPDATE,
                null
        );

//        return updatedTopic;
    }

    //get current user of the system based on security context
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //get topic id from the url hopefully
    private Topic getCurrentTopic(String topicId) {
        return topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + topicId));
    }

    private List<Resource> getTopicResources(TopicDTO topicDTO) {
        /// Upload resources and create embedded Resource objects
        List<Resource> topicResources = List.of();
        if (topicDTO.getResources() != null) {
            topicResources = topicDTO.getResources().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        String url = storeResources.saveFiles(file);
                        ResourceType type = storeResources.determineResourceType(Objects.requireNonNull(file.getOriginalFilename()));
                        return Resource.builder()
                                .type(type)
                                .contentUrl(url)
                                .build();
                    })
                    .toList();
        }
        return topicResources;
    }
}



