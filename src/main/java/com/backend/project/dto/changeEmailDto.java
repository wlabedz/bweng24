package com.backend.project.dto;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

public record changeEmailDto (
    @NotBlank String newEmail,
    @NotBlank String password
)
{}
