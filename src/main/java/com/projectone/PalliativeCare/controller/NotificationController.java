package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.model.Notification;
import com.projectone.PalliativeCare.model.NotificationType;
import com.projectone.PalliativeCare.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications for current user
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications() {
        List<Notification> notifications = notificationService.getUserNotifications();

        ApiResponse<List<Notification>> response = ApiResponse.<List<Notification>>builder()
                .status(HttpStatus.OK)
                .message("Notifications retrieved successfully")
                .data(notifications)
                .build();

        return ResponseEntity.ok(response);
    }

    // Get unread notification count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();

        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .status(HttpStatus.OK)
                .message("Unread count retrieved")
                .data(count)
                .build();

        return ResponseEntity.ok(response);
    }

    // Mark notification as read
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);

        ApiResponse<Notification> response = ApiResponse.<Notification>builder()
                .status(HttpStatus.OK)
                .message("Notification marked as read")
                .data(notification)
                .build();

        return ResponseEntity.ok(response);
    }

    // Doctor endpoints for sending notifications

    // Broadcast to all users
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<String>> broadcastToAll(
            @RequestBody BroadcastRequest request) {

        notificationService.broadcastToAllUsers(
                request.getTitle(),
                request.getMessage(),
                NotificationType.SYSTEM_ALERT
        );

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Notification broadcasted to all users")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // Send to specific topic subscribers
    @PostMapping("/topic/{topicId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<String>> notifyTopicSubscribers(
            @PathVariable String topicId,
            @RequestBody NotificationRequest request) {

        notificationService.sendToTopicSubscribers(
                topicId,
                request.getTitle(),
                request.getMessage(),
                NotificationType.TOPIC_UPDATE,
                null
        );

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Notification sent to topic subscribers")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // Send to all doctors
    @PostMapping("/doctors")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<String>> notifyAllDoctors(
            @RequestBody NotificationRequest request) {

        notificationService.sendToAllDoctors(
                request.getTitle(),
                request.getMessage(),
                NotificationType.DOCTOR_MESSAGE
        );

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Notification sent to all doctors")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // DTOs for request bodies
    @Data
    public static class BroadcastRequest {
        private String title;
        private String message;
    }

    @Data
    public static class NotificationRequest {
        private String title;
        private String message;
    }
}