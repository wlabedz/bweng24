package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record FoundItemDto(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 50) String category,
        @NotBlank @Size(max = 300) String description,
        @NotBlank @Size(max = 30) String office,
        @NotBlank String photo,
        @NotNull @PastOrPresent LocalDate foundDate,
        @NotBlank String foundPlace
) {}
