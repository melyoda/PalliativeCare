package com.projectone.PalliativeCare.controller;


import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.dto.TopicDTO;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.service.TopicServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicServices topicServices;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createTopic(@ModelAttribute TopicDTO topicDTO) {
        try {
            topicServices.createTopic(topicDTO);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.CREATED)
                    .message("Topic created")
                    .data(topicDTO.getTitle())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            // Generic error handler for unexpected issues during registration
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to create topic: " + e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Topic>>> getAllTopics() {
        try {
            List<Topic> topics = topicServices.listAllTopics();

            ApiResponse<List<Topic>> response = ApiResponse.<List<Topic>>builder()
                    .status(HttpStatus.OK)
                    .message("Topics retrieved successfully")
                    .data(topics)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e) {
            ApiResponse<List<Topic>> errorResponse = ApiResponse.<List<Topic>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error retrieving topics: " + e.getMessage())
                    .data(null)
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Topic>>> searchTopics(@RequestParam String keyword) {
        try {
            List<Topic> topics = topicServices.searchTopics(keyword);

            ApiResponse<List<Topic>> response = ApiResponse.<List<Topic>>builder()
                    .status(HttpStatus.OK)
                    .message("Search results for: " + keyword)
                    .data(topics)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<Topic>> errorResponse = ApiResponse.<List<Topic>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error retrieving topics: " + e.getMessage())
                    .data(null)
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
