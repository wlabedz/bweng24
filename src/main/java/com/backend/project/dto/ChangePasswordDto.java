package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordDto(@NotBlank String oldPassword, @NotBlank String newPassword, @NotBlank String confirmPassword){}