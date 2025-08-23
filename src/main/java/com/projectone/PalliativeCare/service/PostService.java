package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.CommentDTO;
import com.projectone.PalliativeCare.dto.PostDTO;
import com.projectone.PalliativeCare.exception.FileUploadException;
import com.projectone.PalliativeCare.exception.InvalidRequestException;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.exception.UnauthorizedActionException;
import com.projectone.PalliativeCare.model.*;
import com.projectone.PalliativeCare.repository.PostRepository;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final TopicRepository topicRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivityService activityService;

    @Value("${app.qa-topic-name:QA}")
    private String qaTopicName;


    // Create post

    /**
     * @deprecated
     * create a post using the DTO
     * @param dto : PostDTO
     * @return postRepo
     *
     */
    public Posts createPost(PostDTO dto) {
        User creator = getCurrentUser();

        // Check if topic exists
        Topic topic = topicRepo.findById(dto.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + dto.getTopicId()));

        Posts post = Posts.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .topicId(topic.getId())
                .resources(getPostResources(dto))
                .createdBy(creator.getId())
                .creationDate(LocalDateTime.now())
                .build();

        Posts saved = postRepo.save(post);
        activityService.logActivity(
                creator.getId(),
                ActivityType.POST_CREATE,
                post.getId(),
                "Creation of post : "+ post.getTitle());

        return saved;
    }

    /**
     *
     * @param topicId : topic id gotten from the url
     * @param postDTO : dto
     * @return postRepo
     */
    public Posts createPost(String topicId, PostDTO postDTO) {
        User creator = getCurrentUser();

        // Check if topic exists
        Topic topic = getCurrentTopic(topicId);

        Posts post = Posts.builder()
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .topicId(topic.getId())
                .resources(getPostResources(postDTO))
                .createdBy(creator.getId())
                .creationDate(LocalDateTime.now())
                .build();

        Posts saved = postRepo.save(post);
        activityService.logActivity(
                creator.getId(),
                ActivityType.POST_CREATE,
                post.getId(),
                "Creation of post : "+ post.getTitle());

        // ✅ Send notification to all topic subscribers
        notificationService.sendToTopicSubscribers(
                topicId,
                "New Post in " + topic.getTitle(),
                "New post: " + postDTO.getTitle(),
                NotificationType.NEW_POST,
                saved.getId()
        );
        return saved;
    }
    /********************************************/
    public Posts patientAddHelpNeeded(PostDTO post) {
        User creator = getCurrentUser();
        Posts patientPost = createPost(getCurrentTopicByTitle(qaTopicName).getId(), post);

        activityService.logActivity(
                creator.getId(),
                ActivityType.POST_CREATE,
                patientPost.getId(),
                "Creation of QA post : "+ post.getTitle());

        return postRepo.save(patientPost);
    }

    public List<Posts> getPostsByCreator() {
        User creator = getCurrentUser();

        return postRepo.findByCreatedBy(creator.getId());
    }

    public List<Posts> getQAPostsByCreator() {
        User creator = getCurrentUser();
        Topic qaTopic = getCurrentTopicByTitle("QA");

        return postRepo.findByCreatedByAndTopicId(creator.getId(), qaTopic.getId());
    }
//**********************************************************************

    /**
     * @param postId: gotten from url
     */
    public void deletePost(String postId) {
        User currentUser = getCurrentUser();
        Posts post = getCurrentPost(postId);

        if(!Objects.equals(currentUser.getId(), post.getCreatedBy())) {
            throw new UnauthorizedActionException("You do not have permission to delete this post");
        }
        postRepo.deleteById(postId);

        activityService.logActivity(
                currentUser.getId(),
                ActivityType.POST_DELETE,
                post.getId(),
                String.format("Deleted post: %s", post.getTitle()));

    }

    // Fetch posts for a topic
    /**
     * this one is used when opening a topic will get the topic id from the url
     * @param topicId : gotten from the url
     * @return a list of posts under this topic
     */
    public List<Posts> getPostsByTopic(String topicId) {
        Topic topic = getCurrentTopic(topicId);
        return postRepo.findByTopicId(topicId);
    }

    /**
     * is used to get posts using the topic name
     * @param topicName: gotten from url param
     * @return first post from the topic
     */
    //check later for improvements
    public List<Posts> getPostsByTopicName(String topicName) {
//        List<Topic> topics = topicRepo.findByTitleContainingIgnoreCase(topicName);
//        if (topics.isEmpty()) throw new ResourceNotFoundException("Topic not found with name: " + topicName);
//        Topic topic = topics.get(0);

        Topic topics = getCurrentTopicByTitle(topicName);
        return postRepo.findByTopicId(topics.getId());
    }

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
//        List<Posts.Comment> comments = post.getComments();
//        Posts.Comment newComment = comments.get(comments.size() - 1); // last added comment
        Posts updatedPost = postRepo.save(post);

        activityService.logActivity(
                creator.getId(),
                ActivityType.COMMENT_CREATE,
                post.getId(),
                "Creation of comment: " + newComment.getText()
        );

        // ✅ Notify the post author about the new comment
        // (unless the commenter is the post author themselves)
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
    //later make delete comment also might migrate comments to its own entity with db palce maybe

    /*
     * Some helper methods
     */

    //get current user of the system based on security context
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername();
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //get topic id from the url hopefully
    private Topic getCurrentTopic(String topicId) {
        return topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + topicId));
    }
    private Topic getCurrentTopicByTitle(String topicTitle) {
        return topicRepo.findFirstByTitle(topicTitle)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with title: " + topicTitle));
    }
    private Posts getCurrentPost(String postId) {
        return postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
    }

    private List<Resource> getPostResources(PostDTO postDTO) {
        List<Resource> resources = List.of(); // empty list if none
        if (postDTO.getResources() != null) {
            resources = postDTO.getResources().stream()
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
        return resources;
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
