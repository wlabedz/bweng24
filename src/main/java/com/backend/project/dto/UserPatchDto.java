package com.backend.project.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserPatchDto {

    private static final Set<String> ISO_COUNTRY_CODES = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toSet());

    @Size(min=5, message = "Username should not be shorter than 5 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    private String name;

    private String surname;
    @Email
    private String mail;
    private String photo;
    private String salutation;
    @Size(min = 2, max = 3, message = "Country code must be contain at least 2 and max 3 characters")
    private String country;


    public UserPatchDto(String username, String name, String surname, String mail, String salutation, String country) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.salutation = salutation;

        if (country != null) {
            if (!ISO_COUNTRY_CODES.contains(country.toUpperCase())) {
                throw new IllegalArgumentException("Invalid ISO country code: " + country);
            }

            this.country = country;
        }
    }
}