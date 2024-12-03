package com.backend.project.dto;

        import jakarta.validation.constraints.NotBlank;
        import jakarta.validation.constraints.Pattern;
        import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank String oldPassword,
        @NotBlank
        @Size(min = 8, max = 12, message = "Password must be between 8 and 12 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$",
                message = "Password must contain at least one uppercase letter and one special character")
        String newPassword,
        @NotBlank String confirmPassword
){}