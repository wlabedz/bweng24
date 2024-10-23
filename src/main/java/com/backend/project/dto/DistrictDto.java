package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DistrictDto(@NotNull int id, @NotBlank String name){}
