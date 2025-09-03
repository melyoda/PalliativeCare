package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.dto.EnrichedPostDTO;
import com.projectone.PalliativeCare.model.Posts;
import com.projectone.PalliativeCare.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/forYou")
@RequiredArgsConstructor
public class MainPageController {
    private final PostService postService;

    /**
     * Get posts from all subscribed topics (For You feed)// only latest 20 posts for now
     */
    @GetMapping("/subscribed")
    public ResponseEntity<ApiResponse<List<EnrichedPostDTO>>> getEnrichedPostsFromSubscribedTopics() {
        List<EnrichedPostDTO> posts = postService.getEnrichedPostsFromSubscribedTopics();

        ApiResponse<List<EnrichedPostDTO>> response = ApiResponse.<List<EnrichedPostDTO>>builder()
                .status(HttpStatus.OK)
                .message("Enriched posts from subscribed topics")
                .data(posts)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-posts")
    public ResponseEntity<ApiResponse<List<EnrichedPostDTO>>> getPostsFromUser() {
        List<EnrichedPostDTO> posts = postService.getPostsByCreator();

        ApiResponse<List<EnrichedPostDTO>> response = ApiResponse.<List<EnrichedPostDTO>>builder()
                .status(HttpStatus.OK)
                .message("Enriched posts made by user")
                .data(posts)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subscribed/paged")
    public ResponseEntity<ApiResponse<Page<Posts>>> getPostsFromSubscribedTopicsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
        Page<Posts> posts = postService.getPostsFromSubscribedTopics(pageable);

        ApiResponse<Page<Posts>> response = ApiResponse.<Page<Posts>>builder()
                .status(HttpStatus.OK)
                .message("Posts from subscribed topics")
                .data(posts)
                .build();

        return ResponseEntity.ok(response);
    }



}
