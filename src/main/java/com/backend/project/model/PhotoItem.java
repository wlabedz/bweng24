package com.backend.project.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document("photos_items")
public class PhotoItem {

    @Id
    private UUID id;

    private String externalId;

    private String name;

    private String contentType;

    private LocalDateTime uploadedAt;

    public PhotoItem(String externalId){
        setId(UUID.randomUUID());
        setExternalId(externalId);
        setUploadedAt(LocalDateTime.now());
    }
}
