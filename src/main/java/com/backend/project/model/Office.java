package com.backend.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection="Offices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Office {

    @Id
    private UUID id;

    private District district;

    private String phoneNumber;
    private String address;
    private String photo;
    private String description;

    public Office(District district, String phoneNumber, String address, String photo, String description){
        this.id = UUID.randomUUID();
        this.district = district;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.photo = photo;
        this.description = description;
    }
}
