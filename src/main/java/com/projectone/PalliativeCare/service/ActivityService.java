package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.model.Activity;
import com.projectone.PalliativeCare.model.ActivityType;
import com.projectone.PalliativeCare.repository.ActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {
    private final ActivityRepository activityRepo;

    public ActivityService(ActivityRepository activityRepo) {
        this.activityRepo = activityRepo;
    }

    public void logActivity(String userId, ActivityType type, String targetId, String targetDescription) {
        Activity activity = Activity.builder()
                .userId(userId)
                .type(type)
                .targetId(targetId)
                .targetDescription(targetDescription)
                .build();

        activityRepo.save(activity);
    }
}
