package com.backend.project.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("user_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhoto {

    @Id
    private UUID id;
    private String content;

    private LocalDateTime uploadedAt;

    public UserPhoto(String content){
        this.id = UUID.randomUUID();
        this.content = content;
        this.uploadedAt = LocalDateTime.now();
    }
}