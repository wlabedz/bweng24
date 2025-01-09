package com.backend.project.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserPatchDto {

    @Size(min=5, message = "Username should not be shorter than 5 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "Name can only contain letters")
    private String name;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "Surname can only contain letters")
    private String surname;

    @Email
    private String mail;

    private String salutation;

    @Size(min = 2, max = 3, message = "Country code must be contain at least 2 and max 3 characters")
    private String country;
}
