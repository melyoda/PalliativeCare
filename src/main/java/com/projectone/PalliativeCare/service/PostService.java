package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.AuthorDTO;
import com.projectone.PalliativeCare.dto.EnrichedPostDTO;
import com.projectone.PalliativeCare.dto.PostDTO;
import com.projectone.PalliativeCare.dto.TopicInfoDTO;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.exception.UnauthorizedActionException;
import com.projectone.PalliativeCare.mapper.PostMapperService;
import com.projectone.PalliativeCare.model.*;
import com.projectone.PalliativeCare.repository.PostRepository;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import com.projectone.PalliativeCare.utils.StoreResources;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final TopicRepository topicRepo;
    private final NotificationService notificationService;
    private final ActivityService activityService;
    private final PostMapperService postMapperService;
    private final StoreResources storeResources;


    @Value("${app.qa-topic-name:QA}")
    private String qaTopicName;


    // Create post
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

    public List<EnrichedPostDTO> getPostsByCreator() {
        User creator = getCurrentUser();
        List<Posts> userPosts = postRepo.findByCreatedBy(creator.getId());
        return postMapperService.toEnrichedPostDTOList(userPosts);
    }

    public List<Posts> getQAPostsByCreator() {
        User creator = getCurrentUser();
        Topic qaTopic = getCurrentTopicByTitle("QA");
        return postRepo.findByCreatedByAndTopicId(
                creator.getId(),
                qaTopic.getId()
        );
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
    public Page<Posts> getPostsByTopic(String topicId, Pageable pageable) {
        Topic topic = getCurrentTopic(topicId);
        return postRepo.findByTopicIdOrderByCreationDateDesc(topic.getId(), pageable);
    }

    /**
     * is used to get posts using the topic name
     * @param topicName: gotten from url param
     * @return first post from the topic
     */
    //check later for improvements
    public List<Posts> getPostsByTopicName(String topicName) {
        Topic topics = getCurrentTopicByTitle(topicName);
        return postRepo.findByTopicId(topics.getId());
    }

    /**
     * Get all posts from topics the current user is subscribed to
     * @return List of posts from subscribed topics
     */

    public List<Posts> getPostsFromSubscribedTopics() {
        User currentUser = getCurrentUser();

        // Get all topic IDs the user is subscribed to
        List<String> subscribedTopicIds = currentUser.getRegisteredTopics();

        if (subscribedTopicIds.isEmpty()) {
            return Collections.emptyList(); // Return empty list if no subscriptions
        }

        // Get all posts from subscribed topics, ordered by creation date (newest first)
        return postRepo.findTop20ByTopicIdInOrderByCreationDateDesc(subscribedTopicIds);

    }

    /**
     * Get enriched posts from subscribed topics
     */
    public List<EnrichedPostDTO> getEnrichedPostsFromSubscribedTopics() {
        List<Posts> posts = getPostsFromSubscribedTopics();
        return postMapperService.toEnrichedPostDTOList(posts);
    }

    /**
     * Get paginated posts from subscribed topics
     */
    public Page<Posts> getPostsFromSubscribedTopics(Pageable pageable) {
        User currentUser = getCurrentUser();
        List<String> subscribedTopicIds = currentUser.getRegisteredTopics();

        if (subscribedTopicIds.isEmpty()) {
            return Page.empty(); // Return empty page if no subscriptions
        }

        return postRepo.findByTopicIdInOrderByCreationDateDesc(subscribedTopicIds, pageable);
    }

    // OLD:
// public Page<Posts> getPostsFromSubscribedTopics(Pageable pageable)
    public Page<EnrichedPostDTO> getPostsFromSubscribedTopicsEnriched(Pageable pageable) {
        User currentUser = getCurrentUser();
        List<String> subscribedTopicIds = currentUser.getRegisteredTopics();

        if (subscribedTopicIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Posts> postsPage =
                postRepo.findByTopicIdInOrderByCreationDateDesc(subscribedTopicIds, pageable);

        // Map each Posts -> EnrichedPostDTO using your private mapper
        return postsPage.map(this::mapToEnrichedPostDTO);
    }

    // OLD:
// public Page<Posts> getPostsByTopic(String topicId, Pageable pageable)
    public Page<EnrichedPostDTO> getPostsByTopicEnriched(String topicId, Pageable pageable) {
        Topic topic = getCurrentTopic(topicId);
        Page<Posts> postsPage =
                postRepo.findByTopicIdOrderByCreationDateDesc(topic.getId(), pageable);

        return postsPage.map(this::mapToEnrichedPostDTO);
    }

    /**
     * Get single enriched post
     */
    public EnrichedPostDTO getEnrichedPost(String postId) {
        Posts post = getCurrentPost(postId);
        return postMapperService.toEnrichedPostDTO(post);
    }

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
        List<Resource> resources = List.of();
        if (postDTO.getResources() != null) {
            resources = postDTO.getResources().stream()
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
        return resources;
    }

    private EnrichedPostDTO mapToEnrichedPostDTO(Posts p) {
        // Look up author
        User author = userRepo.findById(p.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Author not found: " + p.getCreatedBy()));
        Topic topic = topicRepo.findById(p.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found: " + p.getTopicId()));

        AuthorDTO authorDTO = null;
        if (author != null) {
            authorDTO = AuthorDTO.builder()
                    .authorId(author.getId())
                    .authorFirstName(author.getFirstName())
                    .authorLastName(author.getLastName())
                    .authorRole(author.getRole())
                    .build();
        }

        TopicInfoDTO topicDTO = null;
        if (topic != null) {
            topicDTO = TopicInfoDTO.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getTitle())
                    .build();
        }

        return EnrichedPostDTO.builder()
                .postId(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .creationDate(p.getCreationDate())
                .modificationDate(p.getModificationDate())
                .author(authorDTO)
                .topic(topicDTO)
                .resources(p.getResources())
                .commentCount(p.getComments() != null ? p.getComments().size() : 0)
                .build();
    }


}
