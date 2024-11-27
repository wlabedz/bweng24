package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record FoundItemDto(@NotBlank String name, @NotBlank String category, @NotBlank String description, @NotBlank String office, @NotBlank String photo, @NotNull LocalDate foundDate, @NotBlank String foundPlace){}
