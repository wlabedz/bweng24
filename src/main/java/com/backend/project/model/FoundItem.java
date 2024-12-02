package com.backend.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Document(collection="Items")
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
    private String photo;
    private LocalDate foundDate;
    private String foundPlace;
    private UserEntity user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FoundItem(UserEntity user, String name, String category, String description, String office, String photo, LocalDate foundDate, String foundPlace){
        this.id = UUID.randomUUID();
        this.name = name;
        this.category = category;
        this.description = description;
        this.office = office;
        this.photo = photo;
        this.foundDate = foundDate;
        this.foundPlace = foundPlace;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }

    public LocalDate getFoundDate() { return foundDate; }
    public void setFoundDate(LocalDate date) { this.foundDate = date; }

    public String getFoundPlace() { return foundPlace; }
    public void setFoundPlace(String place) { this.foundPlace = place; }

    public UUID getId() { return id; }
    //public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getUpdatedAt(){ return updatedAt;}

    public void setUpdatedAt(LocalDateTime date) { this.updatedAt = date; }

    public LocalDateTime getCreatedAt(){ return createdAt;}

    public void setCreatedAt(LocalDateTime date) { this.createdAt = date; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
