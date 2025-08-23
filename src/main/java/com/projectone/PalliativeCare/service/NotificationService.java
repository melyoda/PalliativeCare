package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.model.*;
import com.projectone.PalliativeCare.repository.NotificationRepository;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // This comes from Spring WebSocket

    @Autowired
    private UserRepository userRepository;

    // Send notification to single user
    public Notification sendToUser(String userId, String title, String message,
                                   NotificationType type, String topicId, String postId) {

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .relatedTopicId(topicId)
                .relatedPostId(postId)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepo.save(notification);

        // Send via WebSocket for real-time updates
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                savedNotification
        );

        return savedNotification;
    }

    // Send to all users in a topic (IMPROVED - YOUR APPROACH)
    public void sendToTopicSubscribers(String topicId, String title, String message,
                                       NotificationType type, String postId) {

        // Get topic with registered users
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        // Send to each user in the topic's registeredUsers list
        for (String userId : topic.getRegisteredUsers()) {
            sendToUser(userId, title, message, type, topicId, postId);
        }
    }

    // Send to all users
    public void broadcastToAllUsers(String title, String message, NotificationType type) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            sendToUser(user.getId(), title, message, type, null, null);
        }
    }

    // Send to all doctors (USING YOUR ROLE ENUM)
    public void sendToAllDoctors(String title, String message, NotificationType type) {
        List<User> doctors = userRepository.findByRole(Role.DOCTOR); // Assuming Role is an enum

        for (User doctor : doctors) {
            sendToUser(doctor.getId(), title, message, type, null, null);
        }
    }

    // Mark notification as read
    public Notification markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepo.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        return notificationRepo.save(notification);
    }

    // Get user notifications
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Get unread count
    public long getUnreadCount(String userId) {
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }
}