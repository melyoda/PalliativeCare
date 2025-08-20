package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.exception.InvalidRequestException;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.exception.UserAlreadyExistsException;
import com.projectone.PalliativeCare.model.ActivityType;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationService {

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ActivityService activityService;

    public void registerUserToTopic(String topicId) {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User currentUser = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
    }

    public void unregisterUserFromTopic(String topicId) {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User currentUser = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
    }
}