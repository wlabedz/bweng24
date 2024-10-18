package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OfficeDto(@NotNull int districtNumber, @NotBlank String phoneNumber,
                        @NotBlank String address,@NotBlank String photo, @NotBlank String description){}
