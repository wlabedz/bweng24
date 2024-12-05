package com.backend.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private UUID id;

    private String name;

    private String surname;

    private String username;

    private String mail;

    private String password;

    private String salutation;

    private UUID photo;

    private String country;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<Roles> roles = new ArrayList<>();

    public UserEntity(String name, String surname, String mail, String username, String password, String salutation, String country){
        this.id = UUID.randomUUID();
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.username = username;
        this.password = password;
        this.salutation = salutation;
        this.photo = null;
        this.country = country;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }
}
