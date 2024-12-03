package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FaqDto(
        @NotBlank @Size(max = 200) String question,
        @NotBlank String answer
) {}