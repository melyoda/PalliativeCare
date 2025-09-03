package com.projectone.PalliativeCare.controller;


import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.dto.TopicDTO;
import com.projectone.PalliativeCare.exception.ResourceNotFoundException;
import com.projectone.PalliativeCare.exception.UnauthorizedActionException;
import com.projectone.PalliativeCare.model.Topic;
import com.projectone.PalliativeCare.service.RegistrationService;
import com.projectone.PalliativeCare.service.TopicServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicServices topicServices;


    private final RegistrationService registrationService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
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

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<String>> deleteTopic(@PathVariable String id) {
        topicServices.deleteTopic(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Topic deleted")
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<String>> updateTopic(
            @PathVariable String id,
            @ModelAttribute TopicDTO topicDTO) {

        topicServices.updateTopic(id, topicDTO);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Topic updated")
                .data(topicDTO.getTitle() != null ? topicDTO.getTitle() : "No title change")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
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

    @PostMapping("{topicId}/register")
    public ResponseEntity<ApiResponse<String>> registerTopic(@PathVariable String topicId) {
//          topicServices.registerUserToTopic(topicId);
            registrationService.registerUserToTopic(topicId);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.OK)
                    .message("User registered to topic successfully")
                    .data(null)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{topicId}/unregister")
    public ResponseEntity<ApiResponse<String>> unregisterUser(@PathVariable String topicId) {
//      topicServices.unregisterUserFromTopic(topicId);
        registrationService.unregisterUserFromTopic(topicId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("User unregistered to topic successfully")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/subscribed")
    public ResponseEntity<ApiResponse<List<Topic>>> getSubscribedTopicsForCurrentUser() {
        try {
            List<Topic> topics = registrationService.getSubscribedTopicsForCurrentUser();

            ApiResponse<List<Topic>> response = ApiResponse.<List<Topic>>builder()
                    .status(HttpStatus.OK)
                    .message("Subscribed topics retrieved successfully")
                    .data(topics)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Log the error for debugging on the server side
//            e.printStackTrace();
            ApiResponse<List<Topic>> errorResponse = ApiResponse.<List<Topic>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error retrieving subscribed topics: " + e.getMessage())
                    .data(new ArrayList<>()) // Ensure data is an empty list on error for consistent Flutter parsing
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
