package com.projectone.PalliativeCare.service;

import com.projectone.PalliativeCare.dto.CommentDTO;
import com.projectone.PalliativeCare.dto.PostDTO;
import com.projectone.PalliativeCare.exception.InvalidRequestException;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.model.Posts;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.PostRepository;
import com.projectone.PalliativeCare.repository.TopicRepository;
import com.projectone.PalliativeCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final TopicRepository topicRepo;

    // Create post
    public Posts createPost(PostDTO dto) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        User creator  = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if topic exists
        Topic topic = topicRepo.findById(dto.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + dto.getTopicId()));

        Posts post = Posts.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .topicId(topic.getId())
                .createdBy(creator.getId())
                .creationDate(LocalDateTime.now())
                .build();

        return postRepo.save(post);
    }

    public Posts createPost(String topicId, PostDTO dto) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername();
        User creator  = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if topic exists
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + topicId));

        Posts post = Posts.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .topicId(topic.getId())
                .createdBy(creator.getId())
                .creationDate(LocalDateTime.now())
                .build();

        return postRepo.save(post);
    }



    // Fetch posts for a topic
    public List<Posts> getPostsByTopic(String topicId) {
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + topicId));
        return postRepo.findByTopicId(topicId);
    }

    public List<Posts> getPostsByTopicName(String topicName) {
        List<Topic> topics = topicRepo.findByTitleContainingIgnoreCase(topicName);
        if (topics.isEmpty()) throw new ResourceNotFoundException("Topic not found with name: " + topicName);
        Topic topic = topics.get(0); // pick the first match
        return postRepo.findByTopicId(topic.getId());
    }

    // Add comment
    public Posts addComment(String postId, CommentDTO dto) {
        Posts post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getText() == null || dto.getText().isEmpty()) {
            throw new InvalidRequestException("Comment text cannot be empty");
        }

        post.addComment(user.getId(), user.getFirstName() + user.getLastName(), dto.getText());
        return postRepo.save(post);
    }

}
