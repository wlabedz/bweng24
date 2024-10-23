package com.backend.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;
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

    public Review(UserEntity user, String opinion){
        this.id = UUID.randomUUID();
        this.user = user;
        this.opinion = opinion;
    }
}
