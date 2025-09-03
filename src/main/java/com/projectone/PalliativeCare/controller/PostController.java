package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.dto.CommentDTO;
import com.projectone.PalliativeCare.dto.EnrichedPostDTO;
import com.projectone.PalliativeCare.dto.PostDTO;
import com.projectone.PalliativeCare.model.Posts;
import com.projectone.PalliativeCare.service.CommentService;
import com.projectone.PalliativeCare.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;


    @PostMapping("/{topicId}/create")
    @PreAuthorize("hasRole('DOCTOR')") // Only doctors can create posts
    public ResponseEntity<ApiResponse<Posts>> createPost(
            @PathVariable String topicId,
            @ModelAttribute PostDTO postDTO) {

        Posts post = postService.createPost(topicId, postDTO);

        ApiResponse<Posts> response = ApiResponse.<Posts>builder()
                .status(HttpStatus.CREATED)
                .message("Post created successfully")
                .data(post)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*
     *later make an admin that will create a discussion Topic
     * only admin and doctors see the discussion topic
     * doctors can comment/answer discussion posts
     * patients can only see their post and its Answers
     * make it so this thing here only takes the id of discussion topic
     *
     */
    @PostMapping("/q-a/create")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Posts>> createPatientQuestion(@RequestBody PostDTO postDTO){

        Posts post = postService.patientAddHelpNeeded(postDTO);

        ApiResponse<Posts> response = ApiResponse.<Posts>builder()
                .status(HttpStatus.CREATED)
                .message("Question created successfully")
                .data(post)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/q-a/view")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Posts>>> viewPatientQuestion(){
        List<Posts> posts = postService.getQAPostsByCreator();

        ApiResponse<List<Posts>> response = ApiResponse.<List<Posts>>builder()
                .status(HttpStatus.OK)
                .message("Patient questions")
                .data(posts)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<ApiResponse<Page<EnrichedPostDTO>>> getPostsByTopic(
            @PathVariable String topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
//        Page<Posts> posts = postService.getPostsByTopic(topicId, pageable);
        Page<EnrichedPostDTO> posts = postService.getPostsByTopicEnriched(topicId, pageable);

        ApiResponse<Page<EnrichedPostDTO>> response = ApiResponse.<Page<EnrichedPostDTO>>builder()
                .status(HttpStatus.OK)
                .message("Posts for topic ID: " + topicId)
                .data(posts)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search-posts")
    public ResponseEntity<ApiResponse<List<Posts>>> getPostsByTopicName(@RequestParam String keyword) {
        List<Posts> posts = postService.getPostsByTopicName(keyword);

        ApiResponse<List<Posts>> response = ApiResponse.<List<Posts>>builder()
                .status(HttpStatus.OK)
                .message("Posts for topic: " + keyword)
                .data(posts)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<ApiResponse<Posts>> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);

        ApiResponse<Posts> response = ApiResponse.<Posts>builder()
                .status(HttpStatus.OK)
                .message("Deleted post with id: " + postId)
                .data(null) // still null because post is deleted
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    /**
     * Get single enriched post
     */
    @GetMapping("/{postId}/enriched")
    public ResponseEntity<ApiResponse<EnrichedPostDTO>> getEnrichedPost(@PathVariable String postId) {
        EnrichedPostDTO post = postService.getEnrichedPost(postId);

        ApiResponse<EnrichedPostDTO> response = ApiResponse.<EnrichedPostDTO>builder()
                .status(HttpStatus.OK)
                .message("Enriched post details")
                .data(post)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get paginated posts from subscribed topics
     */
    @GetMapping("/subscribed/paged")
    public ResponseEntity<ApiResponse<Page<EnrichedPostDTO>>> getPostsFromSubscribedTopicsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
//        Page<Posts> posts = postService.getPostsFromSubscribedTopics(pageable);
        Page<EnrichedPostDTO> posts = postService.getPostsFromSubscribedTopicsEnriched(pageable);

        ApiResponse<Page<EnrichedPostDTO>> response = ApiResponse.<Page<EnrichedPostDTO>>builder()
                .status(HttpStatus.OK)
                .message("Posts from subscribed topics")
                .data(posts)
                .build();

        return ResponseEntity.ok(response);
    }

    // Add a comment to a post
    @PostMapping("/{postId}/comment")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')") // Adjust roles as needed
    public ResponseEntity<ApiResponse<Posts>> addComment(
            @PathVariable String postId,
            @RequestBody CommentDTO commentDTO) {

        Posts updatedPost = commentService.addComment(postId, commentDTO);

        ApiResponse<Posts> response = ApiResponse.<Posts>builder()
                .status(HttpStatus.CREATED)
                .message("Comment added successfully")
                .data(updatedPost)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

