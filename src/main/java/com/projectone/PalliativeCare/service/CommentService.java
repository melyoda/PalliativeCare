package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.CommentDTO;
import com.projectone.PalliativeCare.exception.InvalidRequestException;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.model.ActivityType;
import com.projectone.PalliativeCare.model.NotificationType;
import com.projectone.PalliativeCare.model.Posts;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.PostRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepo;
    private final PostRepository postRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivityService activityService;

    // Add comment
    public Posts addComment(String postId, CommentDTO commentDTO) {
        Posts post = getCurrentPost(postId);
        User creator = getCurrentUser();

        if (commentDTO.getText() == null || commentDTO.getText().isEmpty()) {
            throw new InvalidRequestException("Comment text cannot be empty");
        }

        Posts.Comment newComment = post.addComment(
                creator.getId()
                ,creator.getFirstName() +" "+ creator.getLastName()
                ,commentDTO.getText()
        );

        Posts updatedPost = postRepo.save(post);

        activityService.logActivity(
                creator.getId(),
                ActivityType.COMMENT_CREATE,
                post.getId(),
                "Creation of comment: " + newComment.getText()
        );

        // ✅ Notify the post author about the new comment
        if (!creator.getId().equals(post.getCreatedBy())) {
            notificationService.sendToUser(
                    post.getCreatedBy(),
                    "New Response to Your Post",
                    newComment.getUserDisplayName()
                            + " commented on your post: " + post.getTitle(),
                    NotificationType.HELP_RESPONSE,
                    post.getTopicId(),
                    postId
            );
        }

        return updatedPost;
    }

    //idk man i should've made comments its own class
    public void deleteComment(String postId) {
        Posts post = getCurrentPost(postId);
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername();
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Posts getCurrentPost(String postId) {
        return postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
    }
}
