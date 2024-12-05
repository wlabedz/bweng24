package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record FoundItemDto(
        UUID id,
        @NotBlank @Size(max = 30, message = "Name of the item cannot exceed 30 characters") String name,
        @NotBlank @Size(max = 30, message = "Name of the item category cannot exceed 30 characters") String category,
        @NotBlank @Size(max = 300, message = "Description cannot exceed 300 characters") String description,
        @NotBlank String office,
        @NotBlank String photo,
        @NotNull @PastOrPresent LocalDate foundDate,
        @NotBlank String foundPlace
) {}
