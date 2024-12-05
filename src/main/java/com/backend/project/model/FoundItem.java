package com.backend.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "Items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoundItem {
    @Id
    private UUID id;

    private String name;
    private String category;
    private String description;
    private String office;
    private UUID photoId;
    private LocalDate foundDate;
    private String foundPlace;
    private UserEntity user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FoundItem(UserEntity user, String name, String category, String description, String office, UUID photoId, LocalDate foundDate, String foundPlace) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.category = category;
        this.description = description;
        this.office = office;
        this.photoId = photoId;
        this.foundDate = foundDate;
        this.foundPlace = foundPlace;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
}
