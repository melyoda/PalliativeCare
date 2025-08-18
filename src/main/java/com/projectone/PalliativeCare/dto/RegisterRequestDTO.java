package com.projectone.PalliativeCare.dto;

import com.projectone.PalliativeCare.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank
    private String firstName;
    @NotBlank
    private String middleName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String birthDate;
    @NotBlank
    private String mobile;
    @Email
    private String email;
    @NotBlank
    private String address;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;

    private Role role; // "PATIENT" or "DOCTOR"
}