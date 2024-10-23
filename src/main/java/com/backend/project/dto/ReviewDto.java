package com.backend.project.dto;

import com.backend.project.repository.DistrictRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReviewDto(@NotBlank String opinion){}
