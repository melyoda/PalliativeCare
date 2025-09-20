package com.projectone.PalliativeCare.controller;



import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.dto.UserAccountDTO;
import com.projectone.PalliativeCare.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/users") // A dedicated path for user-related data
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAccountDTO>>> getAllUsers(Authentication authentication) {
        try {
            // Get the email of the currently authenticated user
            String currentUserEmail = authentication.getName();

            // Call the new service method to get the list of DTOs
            List<UserAccountDTO> users = userService.getAllUserAccountsExcept(currentUserEmail);

            // Build the successful response using your ApiResponse pattern
            ApiResponse<List<UserAccountDTO>> response = ApiResponse.<List<UserAccountDTO>>builder()
                    .status(HttpStatus.OK)
                    .message("Successfully retrieved users")
                    .data(users)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Build the error response using your ApiResponse pattern
            ApiResponse<List<UserAccountDTO>> errorResponse = ApiResponse.<List<UserAccountDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve users: " + e.getMessage())
                    .build();

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}