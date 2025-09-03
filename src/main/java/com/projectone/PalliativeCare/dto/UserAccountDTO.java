package com.projectone.PalliativeCare.dto;

import com.projectone.PalliativeCare.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDTO {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String birthDate;
    private String mobile;
    private String email;
    private String address;
    private Role role; //



    // Helper method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
