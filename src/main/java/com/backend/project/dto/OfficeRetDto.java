package com.backend.project.dto;

import com.backend.project.repository.DistrictRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OfficeRetDto(@NotNull UUID id, @NotNull DistrictDto district, @NotBlank String phoneNumber,
                           @NotBlank String address, @NotBlank String photo, @NotBlank String description){}
