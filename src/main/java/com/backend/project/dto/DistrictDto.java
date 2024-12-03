package com.backend.project.dto;

import jakarta.validation.constraints.*;

public record DistrictDto(
        @Min(1) @Max(23) int id,
        @NotBlank @Size(max = 30) String name
) {}