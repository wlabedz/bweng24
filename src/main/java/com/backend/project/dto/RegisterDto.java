package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDto(@NotBlank String username, @NotBlank String password){
}
