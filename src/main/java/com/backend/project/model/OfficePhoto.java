package com.backend.project.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("office_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficePhoto {

    @Id
    private UUID id;
    private String content;

    public OfficePhoto(String content){
        this.id = UUID.randomUUID();
        this.content = content;
    }
}