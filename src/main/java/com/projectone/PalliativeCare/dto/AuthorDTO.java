package com.projectone.PalliativeCare.dto;

import com.projectone.PalliativeCare.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    private String authorId;
    private String authorFirstName;
    private String authorLastName;
    private String authorEmail; // Optional: if needed
    private Role authorRole; // Optional: if you want to show doctor/patient badge
}
