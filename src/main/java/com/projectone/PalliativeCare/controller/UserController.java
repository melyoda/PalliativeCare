package com.projectone.PalliativeCare.controller;

import com.projectone.PalliativeCare.dto.ApiResponse;
import com.projectone.PalliativeCare.dto.LoginRequestDTO;
import com.projectone.PalliativeCare.dto.LoginResponseDTO;
import com.projectone.PalliativeCare.dto.RegisterRequestDTO;
import com.projectone.PalliativeCare.exception.UserAlreadyExistsException;
import com.projectone.PalliativeCare.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("register")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> register(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            LoginResponseDTO loginResponse = service.register(registerRequest);

            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.CREATED)
                    .message("User registration successful")
                    .data(loginResponse)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (UserAlreadyExistsException e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.CONFLICT)
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        } catch (Exception e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Registration failed: " + e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO loginResponse = service.login(loginRequest);

            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Login successful")
                    .data(loginResponse)
                    .build();
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Invalid email or password")
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            ApiResponse<LoginResponseDTO> errorResponse = ApiResponse.<LoginResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Login failed: " + e.getMessage())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
