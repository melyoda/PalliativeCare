package com.projectone.PalliativeCare.exception;

import com.projectone.PalliativeCare.dto.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUserAlreadyExistsException(ResourceNotFoundException exception,
                                                                                WebRequest webRequest) {
        ApiErrorResponseDto errorResponseDto = new ApiErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidRequest(InvalidRequestException exception,
                                                                    WebRequest webRequest) {
        ApiErrorResponseDto errorResponseDto = new ApiErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnauthorized(UnauthorizedActionException exception,
                                                                  WebRequest webRequest) {
        ApiErrorResponseDto errorResponseDto = new ApiErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiErrorResponseDto> handleFileUploadException(FileUploadException exception,
                                                                  WebRequest webRequest) {
        ApiErrorResponseDto errorResponseDto = new ApiErrorResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // later to check on

//    // Optional: Handle Spring's built-in multipart exceptions too
//    @ExceptionHandler(MultipartException.class)
//    public ResponseEntity<ApiResponse<String>> handleMultipartException(
//            MultipartException ex) {
//
//        ApiResponse<String> response = ApiResponse.<String>builder()
//                .status(HttpStatus.BAD_REQUEST)
//                .message("Invalid file upload request: " + ex.getMessage())
//                .data(null)
//                .build();
//
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    public ResponseEntity<ApiResponse<String>> handleMaxSizeException(
//            MaxUploadSizeExceededException ex) {
//
//        ApiResponse<String> response = ApiResponse.<String>builder()
//                .status(HttpStatus.PAYLOAD_TOO_LARGE)
//                .message("File size exceeds maximum allowed")
//                .data(null)
//                .build();
//
//        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
//    }
}
