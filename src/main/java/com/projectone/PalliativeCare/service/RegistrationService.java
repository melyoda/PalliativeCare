package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.exception.InvalidRequestException;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.exception.UserAlreadyExistsException;
import com.projectone.PalliativeCare.model.ActivityType;
import com.projectone.PalliativeCare.model.NotificationType;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class RegistrationService {

    private TopicRepository topicRepo;
    private UserRepository userRepo;
    private ActivityService activityService;
    private NotificationService notificationService;

    public void registerUserToTopic(String topicId) {
        // Get current user
        User currentUser = getCurrentUser();
        String userId = currentUser.getId();

        // Find topic
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Check if already registered
        if (topic.getRegisteredUsers().contains(userId) ||
                currentUser.getRegisteredTopics().contains(topicId)) {
            throw new UserAlreadyExistsException("User is already registered for this topic.");
        }

        // Update both sides
        topic.getRegisteredUsers().add(userId);
        currentUser.getRegisteredTopics().add(topicId);

        // Save both
        topicRepo.save(topic);
        userRepo.save(currentUser);

        // Log activity
        activityService.logActivity(userId, ActivityType.TOPIC_REGISTER, topicId, topic.getTitle());
        // ✅ ADD NOTIFICATION
        notificationService.sendToUser(
                userId,
                "Topic Registered Successfully",
                "You are now receiving updates for: " + topic.getTitle(),
                NotificationType.TOPIC_REGISTRATION,
                topicId,
                null
        );
    }

    public void unregisterUserFromTopic(String topicId) {
        // Get current user
        User currentUser = getCurrentUser();
        String userId = currentUser.getId();

        // Find topic
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Check if actually registered
        boolean wasRegistered = topic.getRegisteredUsers().remove(userId) &&
                currentUser.getRegisteredTopics().remove(topicId);

        if (!wasRegistered) {
            throw new InvalidRequestException("User was not registered for this topic.");
        }

        // Save both
        topicRepo.save(topic);
        userRepo.save(currentUser);

        // Log activity
        activityService.logActivity(userId, ActivityType.TOPIC_UNREGISTER, topicId, topic.getTitle());
        // ✅ ADD NOTIFICATION
        notificationService.sendToUser(
                userId,
                "Topic Unregistered",
                "You will no longer receive updates for: " + topic.getTitle(),
                NotificationType.TOPIC_UNREGISTRATION,
                topicId,
                null
        );
    }

    public List<Topic> getSubscribedTopicsForCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No authenticated user found in context");
        }

        List<String> subscribedTopicIds = currentUser.getRegisteredTopics();
        if (subscribedTopicIds == null) {
            throw new RuntimeException("Registered topics list is null for user: " + currentUser.getEmail());
        }

        if (subscribedTopicIds.isEmpty()) {
            return new ArrayList<>();
        }

        return topicRepo.findAllById(subscribedTopicIds);
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}