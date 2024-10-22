package com.backend.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDto(@NotBlank String name, @NotBlank String surname, @NotBlank String username,
                          @NotBlank @Email String mail, @NotBlank String password){
}
