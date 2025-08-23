package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserIdAndReadFalse(String userId);
    Optional<Notification> findByIdAndUserId(String id, String userId);
}