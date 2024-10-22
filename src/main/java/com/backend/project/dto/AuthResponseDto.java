package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer: ";

    public AuthResponseDto(String accessToken){
        this.accessToken = accessToken;
    }
}

