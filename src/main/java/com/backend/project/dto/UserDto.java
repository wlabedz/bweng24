package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDto(@NotBlank String username,
                      @NotBlank String name,
                      @NotBlank String surname,
                      @NotBlank String mail,
                      String photo) {
}
