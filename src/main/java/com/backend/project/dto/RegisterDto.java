package com.backend.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Arrays;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public class RegisterDto{

        private static final Set<String> ISO_COUNTRY_CODES = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toSet());

        @NotBlank
        private final String name;

        @NotBlank
        private final String surname;

        @NotBlank  @Size(min=5, message = "Username should not be shorter than 5 characters")
        private final String username;

        @NotBlank @Email
        private final String mail;

        @Size(min = 8, max = 12, message = "Password must be between 8 and 12 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$",
                message = "Password must contain at least one uppercase letter and one special character")
        private final String password;

        @NotBlank
        private final String salutation;

        @NotBlank
        private final String country;

    public RegisterDto(String name, String surname, String username, String mail, String password, String salutation, String country) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.mail = mail;
        this.password = password;
        this.salutation = salutation;

        if (!ISO_COUNTRY_CODES.contains(country.toUpperCase())) {
            throw new IllegalArgumentException("Invalid ISO country code: " + country);
        }

        this.country = country;
    }
}
