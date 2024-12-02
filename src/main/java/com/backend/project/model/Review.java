package com.backend.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import java.util.UUID;


@Document(collection="Reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    private UUID id;

    private UserEntity user;

    private String opinion;

    private LocalDateTime createdAt;


    public Review(UserEntity user, String opinion){
        this.id = UUID.randomUUID();
        this.user = user;
        this.opinion = opinion;
        this.createdAt = LocalDateTime.now();
    }
}
